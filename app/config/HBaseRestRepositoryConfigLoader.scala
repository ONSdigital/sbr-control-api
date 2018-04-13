package config

import com.typesafe.config.Config
import com.typesafe.sslconfig.util.ConfigLoader
import repository.hbase.HBaseRestRepositoryConfig

object HBaseRestRepositoryConfigLoader extends ConfigLoader[HBaseRestRepositoryConfig] {
  private val RootPath = "db.hbase-rest"

  def load(rootConfig: Config): HBaseRestRepositoryConfig =
    load(rootConfig, RootPath)

  override def load(rootConfig: Config, path: String): HBaseRestRepositoryConfig = {
    val config = rootConfig.getConfig(path)
    HBaseRestRepositoryConfig(
      protocolWithHostname = config.getString("host"),
      port = config.getString("port"),
      namespace = config.getString("namespace"),
      username = config.getString("username"),
      password = config.getString("password"),
      timeout = config.getLong("timeout")
    )
  }
}