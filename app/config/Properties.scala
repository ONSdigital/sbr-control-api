package config

import com.typesafe.config.{ Config, ConfigFactory }
import play.api.Configuration

/**
 * Created by haqa on 28/07/2017.
 */
trait Properties {

  implicit val config: Configuration
  private val propertiesConfig = config.underlying

  //private val config: Config = SBRPropertiesConfiguration.envConfig(ConfigFactory.load())

  lazy val dbConfig = propertiesConfig.getConfig("db")

  lazy val requestTimeout: Int = propertiesConfig.getInt("request.timeout")
  lazy val minKeyLength: Int = propertiesConfig.getInt("search.minKeyLength")

  // db
  lazy val defaultDBInit: String = propertiesConfig.getString("db.default.name")

}
