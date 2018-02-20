package config

import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging

/**
 * Created by haqa on 28/07/2017.
 */
object SBRPropertiesConfiguration extends LazyLogging {

  def envConfig(conf: Config): Config = {
    logger.info(s"Loading configuration")
    conf
  }
}
