package config

import com.typesafe.config.Config

import repository.hbase.unit.enterprise.HBaseRestEnterpriseUnitRepositoryConfig

/*
 * We want a misconfigured server to "fail fast".
 * The Guice module should be configured to use this ConfigLoader during its configure method.
 * If any required key is missing / any value cannot be successfully parsed, an exception should be thrown
 * which will fail the startup of the service (at deployment time).
 */
object HBaseRestEnterpriseUnitRepositoryConfigLoader extends HBaseRestUnitRepositoryConfigLoader[HBaseRestEnterpriseUnitRepositoryConfig] {
  override def load(rootConfig: Config, path: String): HBaseRestEnterpriseUnitRepositoryConfig = {
    val config = rootConfig.getConfig(path)
    HBaseRestEnterpriseUnitRepositoryConfig(config.getString("enterprise.table.name"))
  }
}
