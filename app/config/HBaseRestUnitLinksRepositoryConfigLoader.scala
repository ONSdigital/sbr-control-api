package config

import com.typesafe.config.Config

import uk.gov.ons.sbr.models.unitlinks.UnitLinks

import repository.hbase.unitlinks.HBaseRestUnitLinksRepositoryConfig


object HBaseRestUnitLinksRepositoryConfigLoader extends HBaseRestConfigLoader[HBaseRestUnitLinksRepositoryConfig]{
  override def load(rootConfig: Config, path: String): UnitLinks = {
    val config = rootConfig.getConfig(path)
    HBaseRestUnitLinksRepositoryConfig(config.getString("unit.links.table.name"))
  }
}
