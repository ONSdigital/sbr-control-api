package utils

import com.typesafe.config.{ Config, ConfigFactory }

/**
 * Created by haqa on 28/07/2017.
 */
object Properties {
  def config: Config = SBRPropertiesConfiguration.envConfig(ConfigFactory.load())

  val requestTimeout: Int = config.getInt("request.timeout")
  val minKeyLength: Int = config.getInt("search.minKeyLength")

}
