package config

import com.typesafe.config.Config

import repository.hbase.unit.enterprise.HBaseRestEnterpriseUnitRepositoryConfig

object HBaseRestEnterpriseUnitRepositoryConfigLoader extends HBaseRestUnitRepositoryConfigLoader[HBaseRestEnterpriseUnitRepositoryConfig] {

  override def load(rootConfig: Config, path: String): HBaseRestEnterpriseUnitRepositoryConfig = {
    val config = rootConfig.getConfig(path)
    HBaseRestEnterpriseUnitRepositoryConfig(config.getString("enterprise.table.name"))
  }
}
