package config

import com.typesafe.config.{ ConfigException, ConfigFactory }
import org.scalatest.{ FreeSpec, Matchers }
import repository.hbase.legalunit.HBaseRestLegalUnitRepositoryConfig

class HBaseRestLegalUnitRepositoryConfigLoaderSpec extends FreeSpec with Matchers {

  private trait ValidConfigurationFixture {
    private val SampleConfiguration =
      """|db {
         |  hbase-rest {
         |    enterprise.table.name = "enterprise-table"
         |    unit.links.table.name = "unitlinks-table"
         |    legalunit.table.name = "legalunit-table"
         |  }
         |}""".stripMargin
    val config = ConfigFactory.parseString(SampleConfiguration)
  }

  private trait MissingConfigurationFixture {
    private val SampleConfiguration =
      """|db {
         |  hbase-rest {
         |    enterprise.table.name = "enterprise-table"
         |    unit.links.table.name = "unitlinks-table"
         |    # legalunit.table.name is missing
         |  }
         |}""".stripMargin
    val config = ConfigFactory.parseString(SampleConfiguration)
  }

  "The config for the HBase REST LegalUnit repository" - {
    "can be successfully loaded when valid" in new ValidConfigurationFixture {
      HBaseRestLegalUnitRepositoryConfigLoader.load(config) shouldBe HBaseRestLegalUnitRepositoryConfig("legalunit-table")
    }

    "cannot be loaded when a configuration value is missing" in new MissingConfigurationFixture {
      a[ConfigException] should be thrownBy {
        HBaseRestLegalUnitRepositoryConfigLoader.load(config)
      }
    }
  }
}
