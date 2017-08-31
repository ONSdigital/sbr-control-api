import com.google.inject.AbstractModule
import java.time.Clock

import com.typesafe.config.{ Config, ConfigFactory }
import config.SBRPropertiesConfiguration
import play.api.{ Configuration, Environment }

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
    //    val config: Config = ConfigFactory.load
    bind(classOf[Config]).toInstance(config)

    // Use the system clock as the default implementation of Clock
    bind(classOf[Clock]).toInstance(Clock.systemDefaultZone)
  }

}
