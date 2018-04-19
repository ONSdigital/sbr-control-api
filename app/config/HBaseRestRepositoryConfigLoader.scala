package config

import com.typesafe.config.Config
import repository.hbase.HBaseRestRepositoryConfig

/*
 * We want a misconfigured server to "fail fast".
 * The Guice module should be configured to use this ConfigLoader during its configure method.
 * If any required key is missing / any value cannot be successfully parsed, an exception should be thrown
 * which will fail the startup of the service (at deployment time).
 *
 * Note that we cannot currently enforce that port is numeric, because some environments rely on this
 * being configured with a trailing path, such as "8080/hbase".
 */
object HBaseRestRepositoryConfigLoader extends HBaseRestConfigLoader[HBaseRestRepositoryConfig] {
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