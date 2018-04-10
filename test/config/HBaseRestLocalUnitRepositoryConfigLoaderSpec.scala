package config

import com.typesafe.config.{ ConfigException, ConfigFactory }
import org.scalatest.{ FreeSpec, Matchers }
import repository.hbase.HBaseRestLocalUnitRepositoryConfig

class HBaseRestLocalUnitRepositoryConfigLoaderSpec extends FreeSpec with Matchers {

  private trait ValidConfigurationFixture {
    private val SampleConfiguration =
      """|db {
         |  hbase-rest {
         |    enterprise.table.name = "enterprise-table"
         |    unit.links.table.name = "unitlinks-table"
         |    localunit.table.name = "localunit-table"
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
         |    # localunit.table.name is missing
         |  }
         |}""".stripMargin
    val config = ConfigFactory.parseString(SampleConfiguration)
  }

  "The config for the HBase REST LocalUnit repository" - {
    "can be successfully loaded when valid" in new ValidConfigurationFixture {
      HBaseRestLocalUnitRepositoryConfigLoader.load(config) shouldBe HBaseRestLocalUnitRepositoryConfig("localunit-table")
    }

    "cannot be loaded when a configuration value is missing" in new MissingConfigurationFixture {
      a[ConfigException] should be thrownBy {
        HBaseRestLocalUnitRepositoryConfigLoader.load(config)
      }
    }
  }
}
