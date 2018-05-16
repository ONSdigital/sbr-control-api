package config

import com.typesafe.config.Config
import repository.hbase.reportingunit.HBaseRestReportingUnitRepositoryConfig

object HBaseRestReportingUnitRepositoryConfigLoader extends HBaseRestConfigLoader[HBaseRestReportingUnitRepositoryConfig] {
  override def load(rootConfig: Config, path: String): HBaseRestReportingUnitRepositoryConfig = {
    val config = rootConfig.getConfig(path)
    HBaseRestReportingUnitRepositoryConfig(config.getString("reportingunit.table.name"))
  }
}
