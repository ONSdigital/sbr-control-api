package utils

import com.typesafe.config.{ Config, ConfigFactory }

/**
 * Created by haqa on 28/07/2017.
 */
object Properties {

  private[this] val config: Config = ConfigFactory.load

  val host: String = config.getString("legal.units.source.host")
  val requestTimeout: Int = config.getInt("request.timeout")
  val minKeyLength: Int = config.getInt("minLengthKey")

}
