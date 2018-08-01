import java.time.Clock

import com.google.inject.{ AbstractModule, TypeLiteral }
import config.{ HBaseRestEnterpriseUnitRepositoryConfigLoader, HBaseRestLegalUnitRepositoryConfigLoader, HBaseRestLocalUnitRepositoryConfigLoader, HBaseRestRepositoryConfigLoader, _ }
import kamon.Kamon
import play.api.{ Configuration, Environment }
import repository._
import repository.hbase._
import repository.hbase.enterprise.{ EnterpriseUnitRowMapper, HBaseRestEnterpriseUnitRepository, HBaseRestEnterpriseUnitRepositoryConfig }
import repository.hbase.legalunit.{ HBaseRestLegalUnitRepository, HBaseRestLegalUnitRepositoryConfig, LegalUnitRowMapper }
import repository.hbase.localunit.{ HBaseRestLocalUnitRepository, HBaseRestLocalUnitRepositoryConfig, LocalUnitRowMapper }
import repository.hbase.reportingunit.{ HBaseRestReportingUnitRepository, HBaseRestReportingUnitRepositoryConfig, ReportingUnitRowMapper }
import repository.hbase.unitlinks.{ HBaseRestUnitLinksRepository, HBaseRestUnitLinksRepositoryConfig, UnitLinksRowMapper }
import services.{ DataAccess, HBaseRestDataAccess }
import uk.gov.ons.sbr.models.enterprise.Enterprise
import uk.gov.ons.sbr.models.legalunit.LegalUnit
import uk.gov.ons.sbr.models.localunit.LocalUnit
import uk.gov.ons.sbr.models.reportingunit.ReportingUnit
import uk.gov.ons.sbr.models.unitlinks.UnitLinks

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

    bind(classOf[DataAccess]).to(classOf[HBaseRestDataAccess])
    bind(classOf[RestRepository]).to(classOf[HBaseRestRepository])
    bind(classOf[LocalUnitRepository]).to(classOf[HBaseRestLocalUnitRepository])
    bind(classOf[LegalUnitRepository]).to(classOf[HBaseRestLegalUnitRepository])
    bind(classOf[ReportingUnitRepository]).to(classOf[HBaseRestReportingUnitRepository])
    bind(classOf[EnterpriseUnitRepository]).to(classOf[HBaseRestEnterpriseUnitRepository])
    bind(classOf[UnitLinksRepository]).to(classOf[HBaseRestUnitLinksRepository])
    bind(classOf[HBaseResponseReaderMaker]).toInstance(HBaseResponseReader)
    bind(new TypeLiteral[RowMapper[ReportingUnit]]() {}).toInstance(ReportingUnitRowMapper)
    bind(new TypeLiteral[RowMapper[LocalUnit]]() {}).toInstance(LocalUnitRowMapper)
    bind(new TypeLiteral[RowMapper[LegalUnit]]() {}).toInstance(LegalUnitRowMapper)

    bind(new TypeLiteral[RowMapper[Enterprise]]() {}).toInstance(EnterpriseUnitRowMapper)
    bind(new TypeLiteral[RowMapper[UnitLinks]]() {}).toInstance(UnitLinksRowMapper)
    bind(classOf[Clock]).toInstance(Clock.systemDefaultZone)

    Kamon.loadReportersFromConfig()
  }
}
