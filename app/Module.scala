import com.google.inject.AbstractModule
import java.time.Clock

import com.typesafe.config.{ Config, ConfigFactory }
import config.SBRPropertiesConfiguration
import org.apache.hadoop.util.ToolRunner
import play.api.{ Configuration, Environment }
import services.{ DataAccess, HBaseDataAccess, HBaseDataLoadConfig, HBaseRestDataAccess }
import uk.gov.ons.sbr.data.hbase.HBaseConnector
import uk.gov.ons.sbr.data.hbase.load.BulkLoader

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
class Module(environment: Environment, configuration: Configuration) extends AbstractModule with HBaseDataLoadConfig {
  override def configure() = {
    val config = SBRPropertiesConfiguration.envConfig(ConfigFactory.load())

    // In addition to using -Ddatabase=hbase-in-memory, -Dsbr.hbase.inmemory=true needs to be set to true for
    // HBase in memory to work (this is required by the HBase connector .jar)
    config.getString("db.default") match {
      case "hbase-in-memory" => bind(classOf[DataAccess]).to(classOf[HBaseDataAccess])
      case "hbase-rest" => bind(classOf[DataAccess]).to(classOf[HBaseRestDataAccess])
      case _ => bind(classOf[DataAccess]).to(classOf[HBaseDataAccess])
    }

    // Load the data into HBase (if the correct environment variables have been set)
    if (config.getBoolean("db.load")) {
      val bulkLoader = new BulkLoader()
      if (config.getString("db.default") == "hbase-rest") HBaseConnector.getInstance().connect()

      //   Load in data for first period (201706)
      ToolRunner.run(HBaseConnector.getInstance().getConfiguration, bulkLoader, entData201706.toArray)

      // Load in Links
      ToolRunner.run(HBaseConnector.getInstance().getConfiguration, bulkLoader, entLeu201706.toArray)
      ToolRunner.run(HBaseConnector.getInstance().getConfiguration, bulkLoader, entVat201706.toArray)
      ToolRunner.run(HBaseConnector.getInstance().getConfiguration, bulkLoader, entPaye201706.toArray)
      ToolRunner.run(HBaseConnector.getInstance().getConfiguration, bulkLoader, entCh201706.toArray)
      ToolRunner.run(HBaseConnector.getInstance().getConfiguration, bulkLoader, leuCh201706.toArray)
      ToolRunner.run(HBaseConnector.getInstance().getConfiguration, bulkLoader, leuPaye201706.toArray)
      ToolRunner.run(HBaseConnector.getInstance().getConfiguration, bulkLoader, leuVat201706.toArray)

      // Load in data for second period (201708)
      ToolRunner.run(HBaseConnector.getInstance().getConfiguration, bulkLoader, entData201708.toArray)

      // Load in Links
      ToolRunner.run(HBaseConnector.getInstance().getConfiguration, bulkLoader, entLeu201708.toArray)
      ToolRunner.run(HBaseConnector.getInstance().getConfiguration, bulkLoader, entVat201708.toArray)
      ToolRunner.run(HBaseConnector.getInstance().getConfiguration, bulkLoader, entPaye201708.toArray)
      ToolRunner.run(HBaseConnector.getInstance().getConfiguration, bulkLoader, entCh201708.toArray)
      ToolRunner.run(HBaseConnector.getInstance().getConfiguration, bulkLoader, leuCh201708.toArray)
      ToolRunner.run(HBaseConnector.getInstance().getConfiguration, bulkLoader, leuPaye201708.toArray)
      ToolRunner.run(HBaseConnector.getInstance().getConfiguration, bulkLoader, leuVat201708.toArray)
    }

    bind(classOf[Config]).toInstance(config)
    bind(classOf[Clock]).toInstance(Clock.systemDefaultZone)
  }
}
