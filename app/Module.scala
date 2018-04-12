import java.time.Clock

import play.api.{ Configuration, Environment }
import com.google.inject.{ AbstractModule, TypeLiteral }

import uk.gov.ons.sbr.models.enterprise.Enterprise
import uk.gov.ons.sbr.models.localunit.LocalUnit

import config.{ HBaseRestEnterpriseUnitRepositoryConfigLoader, HBaseRestLocalUnitRepositoryConfigLoader, HBaseRestRepositoryConfigLoader }
import repository.hbase._
import repository.hbase.unit.enterprise.{ EnterpriseUnitRepository, EnterpriseUnitRowMapper, HBaseRestEnterpriseUnitRepository, HBaseRestEnterpriseUnitRepositoryConfig }
import repository.{ LocalUnitRepository, RestRepository, RowMapper }
import services.{ DataAccess, HBaseRestDataAccess }

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
  override def configure() = {
    val underlyingConfig = configuration.underlying
    val hBaseRestConfig = HBaseRestRepositoryConfigLoader.load(underlyingConfig)
    val hBaseRestLocalUnitConfig = HBaseRestLocalUnitRepositoryConfigLoader.load(underlyingConfig)
    val hbaseRestEnterpriseUnitConfig = HBaseRestEnterpriseUnitRepositoryConfigLoader.load(underlyingConfig)
    bind(classOf[HBaseRestRepositoryConfig]).toInstance(hBaseRestConfig)
    bind(classOf[HBaseRestLocalUnitRepositoryConfig]).toInstance(hBaseRestLocalUnitConfig)
    bind(classOf[HBaseRestEnterpriseUnitRepositoryConfig]).toInstance(hbaseRestEnterpriseUnitConfig)

    bind(classOf[DataAccess]).to(classOf[HBaseRestDataAccess])
    bind(classOf[RestRepository]).to(classOf[HBaseRestRepository])
    bind(classOf[LocalUnitRepository]).to(classOf[HBaseRestLocalUnitRepository])
    bind(classOf[EnterpriseUnitRepository]).to(classOf[HBaseRestEnterpriseUnitRepository])
    bind(classOf[HBaseResponseReaderMaker]).toInstance(HBaseResponseReader)
    bind(new TypeLiteral[RowMapper[LocalUnit]]() {}).toInstance(LocalUnitRowMapper)
    bind(new TypeLiteral[RowMapper[Enterprise]]() {}).toInstance(EnterpriseUnitRowMapper)
    bind(classOf[Clock]).toInstance(Clock.systemDefaultZone)
  }
}
