package config

import org.scalatest.{ FreeSpec, Matchers }
import com.typesafe.config.{ Config, ConfigException, ConfigFactory }

import repository.hbase.enterprise.HBaseRestEnterpriseUnitRepositoryConfig

class HBaseRestEnterpriseUnitRepositoryConfigLoaderSpec extends FreeSpec with Matchers {

  private trait ValidConfigurationFixture {
    val SampleConfiguration: String =
      """|db {
         |  hbase-rest {
         |    enterprise.table.name = "enterprise-table"
         |    unit.links.table.name = "unitlinks-table"
         |    localunit.table.name = "localunit-table"
         |  }
         |}""".stripMargin
    val config: Config = ConfigFactory.parseString(SampleConfiguration)
  }

  private trait InvalidConfigurationFixture {
    val SampleConfiguration: String =
      """|db {
         |  hbase-rest {
         |    # enterprise.table.name = "enterprise-table"
         |    unit.links.table.name = "unitlinks-table"
         |    localunit.table.name = "localunit-table"
         |  }
         |}""".stripMargin
    val config: Config = ConfigFactory.parseString(SampleConfiguration)
  }

  "The config for the HBase REST LocalUnit repository" - {
    "can be successfully loaded when valid" in new ValidConfigurationFixture {
      HBaseRestEnterpriseUnitRepositoryConfigLoader.load(config) shouldBe HBaseRestEnterpriseUnitRepositoryConfig("enterprise-table")
    }

    "cannot be loaded when configuration value is missing" in new InvalidConfigurationFixture {
      a[ConfigException] should be thrownBy {
        HBaseRestEnterpriseUnitRepositoryConfigLoader.load(config)
      }
    }
  }

}
