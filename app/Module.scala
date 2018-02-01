import com.google.inject.AbstractModule
import java.time.Clock

import com.typesafe.config.{Config, ConfigFactory}
import config.SBRPropertiesConfiguration
import play.api.{Configuration, Environment}
import services.{DataAccess, HBaseDataAccess}

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
class Module(
    environment: Environment,
    configuration: Configuration
) extends AbstractModule {

  override def configure() = {

    val config = SBRPropertiesConfiguration.envConfig(ConfigFactory.load())

    // In addition to using -Ddatabase=hbase-in-memory, -Dsbr.hbase.inmemory=true needs to be set to true for
    // HBase in memory to work (this is required by the HBase connector .jar)
    config.getString("database") match {
      case "hbase-in-memory" => bind(classOf[DataAccess]).to(classOf[HBaseDataAccess])
    }

    bind(classOf[Config]).toInstance(config)
    // Use the system clock as the default implementation of Clock
    bind(classOf[Clock]).toInstance(Clock.systemDefaultZone)
  }
}
