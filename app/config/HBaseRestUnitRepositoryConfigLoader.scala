package config

import com.typesafe.config.Config
import com.typesafe.sslconfig.util.ConfigLoader

trait HBaseRestUnitRepositoryConfigLoader[T] extends ConfigLoader[T] {
  private val RootPath = "db.hbase-rest"

  def load(rootConfig: Config): T =
    load(rootConfig, RootPath)
}
