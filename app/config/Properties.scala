package config

import com.typesafe.config.{ Config, ConfigFactory }

/**
 * Created by haqa on 28/07/2017.
 */
object Properties {
  private val config: Config = SBRPropertiesConfiguration.envConfig(ConfigFactory.load())

  lazy val requestTimeout: Int = config.getInt("request.timeout")
  lazy val minKeyLength: Int = config.getInt("search.minKeyLength")

}
