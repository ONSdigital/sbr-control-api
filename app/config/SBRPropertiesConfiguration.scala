package config

import org.slf4j.LoggerFactory

import com.typesafe.config.Config

/**
 * Created by haqa on 28/07/2017.
 */
object SBRPropertiesConfiguration {

  private[this] val logger = LoggerFactory.getLogger(SBRPropertiesConfiguration.getClass)

  def envConfig(conf: Config): Config = {
    val env = sys.props.get("environment").getOrElse("default")
    logger.info(s"Load config for [$env] env")
    val envConf = conf.getConfig(s"env.$env")
    logger.debug(envConf.toString)
    envConf
  }

}
