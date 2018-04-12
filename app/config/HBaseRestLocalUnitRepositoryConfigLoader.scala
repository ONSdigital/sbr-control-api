package config

import com.typesafe.config.Config

import repository.hbase.HBaseRestLocalUnitRepositoryConfig

object HBaseRestLocalUnitRepositoryConfigLoader extends HBaseRestUnitRepositoryConfigLoader[HBaseRestLocalUnitRepositoryConfig] {

  //  private val RootPath = "db.hbase-rest"
  //
  //  def load(rootConfig: Config): HBaseRestLocalUnitRepositoryConfig =
  //    load(rootConfig, RootPath)

  override def load(rootConfig: Config, path: String): HBaseRestLocalUnitRepositoryConfig = {
    val config = rootConfig.getConfig(path)
    HBaseRestLocalUnitRepositoryConfig(config.getString("localunit.table.name"))
  }
}
