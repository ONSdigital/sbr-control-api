import java.util.Optional

import Module.Names.{ Paye, UnitLink, Vat }
import Module.closeSilently
import akka.actor.ActorSystem
import com.google.inject.{ AbstractModule, Provides, TypeLiteral }
import config.{ HBaseRestEnterpriseUnitRepositoryConfigLoader, HBaseRestLegalUnitRepositoryConfigLoader, HBaseRestLocalUnitRepositoryConfigLoader, HBaseRestRepositoryConfigLoader, _ }
import handlers.PatchHandler
import handlers.http.HttpPatchHandler
import javax.inject.{ Inject, Named }
import kamon.Kamon
import org.apache.solr.client.solrj.impl.CloudSolrClient
import play.api.inject.ApplicationLifecycle
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.ws.WSClient
import play.api.mvc.Result
import play.api.{ Configuration, Environment }
import repository._
import repository.hbase._
import repository.hbase.enterprise.{ EnterpriseUnitRowMapper, HBaseRestEnterpriseUnitRepository, HBaseRestEnterpriseUnitRepositoryConfig }
import repository.hbase.legalunit.{ HBaseRestLegalUnitRepository, HBaseRestLegalUnitRepositoryConfig, LegalUnitRowMapper }
import repository.hbase.localunit.{ HBaseRestLocalUnitRepository, HBaseRestLocalUnitRepositoryConfig, LocalUnitRowMapper }
import repository.hbase.reportingunit.{ HBaseRestReportingUnitRepository, HBaseRestReportingUnitRepositoryConfig, ReportingUnitRowMapper }
import repository.hbase.unitlinks.{ HBaseRestUnitLinksRepository, HBaseRestUnitLinksRepositoryConfig, UnitLinksNoPeriodRowMapper }
import repository.solr.SolrLegalUnitRepository
import services._
import uk.gov.ons.sbr.models.enterprise.Enterprise
import uk.gov.ons.sbr.models.legalunit.LegalUnit
import uk.gov.ons.sbr.models.localunit.LocalUnit
import uk.gov.ons.sbr.models.reportingunit.ReportingUnit
import uk.gov.ons.sbr.models.unitlinks.UnitLinksNoPeriod

import scala.concurrent.Future

/**
 * This class is a Guice module that tells Guice how to bind several
 * different types. This Guice module is created when the Play
 * application starts.
 *
 * Play will automatically use any class called `Module` that is in
 * the root package. You can create modules in other locations by
 * adding `play.modules.enabled` settings to the `application.conf`
 * configuration file.
 */
class Module(environment: Environment, configuration: Configuration) extends AbstractModule {
  override def configure(): Unit = {
    val underlyingConfig = configuration.underlying
    val hBaseRestConfig = HBaseRestRepositoryConfigLoader.load(underlyingConfig)
    val hBaseRestLocalUnitConfig = HBaseRestLocalUnitRepositoryConfigLoader.load(underlyingConfig)
    val hBaseRestLegalUnitConfig = HBaseRestLegalUnitRepositoryConfigLoader.load(underlyingConfig)
    val hbaseRestEnterpriseUnitConfig = HBaseRestEnterpriseUnitRepositoryConfigLoader.load(underlyingConfig)
    val hbaseRestUnitLinksConfig = HBaseRestUnitLinksRepositoryConfigLoader.load(underlyingConfig)
    val hbaseRestReportingUnitConfig = HBaseRestReportingUnitRepositoryConfigLoader.load(underlyingConfig)
    bind(classOf[HBaseRestRepositoryConfig]).toInstance(hBaseRestConfig)
    bind(classOf[HBaseRestLocalUnitRepositoryConfig]).toInstance(hBaseRestLocalUnitConfig)
    bind(classOf[HBaseRestLegalUnitRepositoryConfig]).toInstance(hBaseRestLegalUnitConfig)
    bind(classOf[HBaseRestEnterpriseUnitRepositoryConfig]).toInstance(hbaseRestEnterpriseUnitConfig)
    bind(classOf[HBaseRestUnitLinksRepositoryConfig]).toInstance(hbaseRestUnitLinksConfig)
    bind(classOf[HBaseRestReportingUnitRepositoryConfig]).toInstance(hbaseRestReportingUnitConfig)

    bind(classOf[RestRepository]).to(classOf[HBaseRestRepository])
    bind(classOf[LocalUnitRepository]).to(classOf[HBaseRestLocalUnitRepository])
    bind(classOf[LegalUnitRepository]).to(classOf[HBaseRestLegalUnitRepository])
    bind(classOf[ReportingUnitRepository]).to(classOf[HBaseRestReportingUnitRepository])
    bind(classOf[EnterpriseUnitRepository]).to(classOf[HBaseRestEnterpriseUnitRepository])
    bind(classOf[HBaseResponseReaderMaker]).toInstance(HBaseResponseReader)

    bind(new TypeLiteral[RowMapper[ReportingUnit]]() {}).toInstance(ReportingUnitRowMapper)
    bind(new TypeLiteral[RowMapper[LocalUnit]]() {}).toInstance(LocalUnitRowMapper)
    bind(new TypeLiteral[RowMapper[LegalUnit]]() {}).toInstance(LegalUnitRowMapper)
    bind(new TypeLiteral[RowMapper[Enterprise]]() {}).toInstance(EnterpriseUnitRowMapper)
    bind(new TypeLiteral[RowMapper[UnitLinksNoPeriod]]() {}).toInstance(UnitLinksNoPeriodRowMapper)
    bind(new TypeLiteral[PatchHandler[Future[Result]]]() {}).to(classOf[HttpPatchHandler])

    Kamon.loadReportersFromConfig()
  }

  @Provides @Named(Vat)
  def providesVatRegisterService(@Inject() wsClient: WSClient): UnitRegisterService = {
    val vatAdminDataServiceUrl = BaseUrlConfigLoader.load(configuration.underlying, "api.admin.data.vat")
    new AdminUnitRegisterService(vatAdminDataServiceUrl, wsClient)
  }

  @Provides @Named(Paye)
  def providesPayeRegisterService(@Inject() wsClient: WSClient): UnitRegisterService = {
    val payeAdminDataServiceUrl = BaseUrlConfigLoader.load(configuration.underlying, "api.admin.data.paye")
    new AdminUnitRegisterService(payeAdminDataServiceUrl, wsClient)
  }

  @Provides @Named(UnitLink)
  def providesUnitLinkRegisterService(
    @Inject() restRepository: RestRepository,
    unitLinksRepositoryConfig: HBaseRestUnitLinksRepositoryConfig
  ): UnitRegisterService =
    new UnitLinkUnitRegisterService(restRepository, unitLinksRepositoryConfig)

  @Provides
  def providesUnitLinksRepository(
    @Inject() restRepository: RestRepository,
    unitLinksRepositoryConfig: HBaseRestUnitLinksRepositoryConfig,
    rowMapper: RowMapper[UnitLinksNoPeriod],
    @Named(UnitLink) unitLinkRegisterService: UnitRegisterService
  ): UnitLinksRepository =
    new HBaseRestUnitLinksRepository(restRepository, unitLinksRepositoryConfig, rowMapper, unitLinkRegisterService)

  @Provides
  def providesPatchService(
    @Inject() unitLinksRepository: UnitLinksRepository,
    @Named(Vat) vatRegisterService: UnitRegisterService,
    @Named(Paye) payeRegisterService: UnitRegisterService,
    @Named(UnitLink) unitLinkRegisterService: UnitRegisterService
  ): PatchService = {
    val adminDataUnitLinkPatchService = new AdminUnitLinkPatchService(unitLinksRepository, unitLinkRegisterService)
    val adminDataRegisterService = new CompositeAdminUnitRegisterService(vatRegisterService, payeRegisterService)
    val legalUnitLinkPatchService = new LegalUnitLinkPatchService(unitLinksRepository, adminDataRegisterService)
    new UnitLinksPatchService(adminDataUnitLinkPatchService, legalUnitLinkPatchService)
  }

  // solr
  //  @Provides
  //  def providesSolrClientBuilder(): CloudSolrClient.Builder = {
  //    import scala.collection.JavaConverters._
  //
  //    val zkHosts = List("localhost:9983")
  //    new CloudSolrClient.Builder(zkHosts.asJava, Optional.empty())
  //  }

  @Provides
  def providesSolrLegalUnitRepository(@Inject() actorSystem: ActorSystem, applicationLifecycle: ApplicationLifecycle): SolrLegalUnitRepository = {
    val solrExecutionContext = actorSystem.dispatchers.lookup("solr-context")

    val zkHosts = List("localhost:9983")
    import scala.collection.JavaConverters._

    // attempt using a single client for all concurrent requests
    val clientBuilder = new CloudSolrClient.Builder(zkHosts.asJava, Optional.empty())
    val client = clientBuilder.build()
    applicationLifecycle.addStopHook(() => Future(closeSilently(client)))

    new SolrLegalUnitRepository(client)(solrExecutionContext)
  }
}

private object Module {
  /*
   * final is required for Scala to emit bytecode that will be considered "constant",
   * so that it can be used in @Named annotations.
   */
  object Names {
    final val Paye = "paye"
    final val UnitLink = "unit-link"
    final val Vat = "vat"
  }

  private def closeSilently(closeable: AutoCloseable): Unit =
    try {
      closeable.close()
    } catch {
      case _: Exception => () // silently ignore
    }
}