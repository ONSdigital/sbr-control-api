package config

import com.typesafe.config.Config
import com.typesafe.sslconfig.util.ConfigLoader
import utils.BaseUrl

/*
 * Note that 'prefix' is used only in a very specific example, and so is not catered for by this
 * general purpose loader.
 */
object BaseUrlConfigLoader extends ConfigLoader[BaseUrl] {
  override def load(rootConfig: Config, path: String): BaseUrl = {
    val config = rootConfig.getConfig(path)
    BaseUrl(
      protocol = config.getString("protocol"),
      host = config.getString("host"),
      port = config.getInt("port"),
      prefix = None
    )
  }
}
