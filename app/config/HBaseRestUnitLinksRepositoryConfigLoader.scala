package config

import com.typesafe.config.Config

import repository.hbase.unitlinks.HBaseRestUnitLinksRepositoryConfig

object HBaseRestUnitLinksRepositoryConfigLoader extends HBaseRestConfigLoader[HBaseRestUnitLinksRepositoryConfig] {
  override def load(rootConfig: Config, path: String): HBaseRestUnitLinksRepositoryConfig = {
    val config = rootConfig.getConfig(path)
    HBaseRestUnitLinksRepositoryConfig(config.getString("unit.links.table.name"))
  }
}
