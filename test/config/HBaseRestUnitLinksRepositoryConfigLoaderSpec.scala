package config

import org.scalatest.{ FreeSpec, Matchers }
import com.typesafe.config.{ ConfigException, ConfigFactory }

import repository.hbase.unitlinks.HBaseRestUnitLinksRepositoryConfig

class HBaseRestUnitLinksRepositoryConfigLoaderSpec extends FreeSpec with Matchers {

  private trait ValidConfigurationFixture {
    private val SampleConfiguraion =
      """|db {
       |  hbase-rest {
       |    enterprise.table.name = "enterprise-table"
       |    unit.links.table.name = "unitlinks-table"
       |    localunit.table.name = "localunit-table"
       |  }
       |}""".stripMargin

    val config = ConfigFactory.parseString(SampleConfiguraion)
  }

  private trait MissingConfigurationFixture {
    private val SampleConfiguraion =
      """|db {
         |  hbase-rest {
         |    enterprise.table.name = "enterprise-table"
         |    #unit.links.table.name = "unitlinks-table"
         |    localunit.table.name = "localunit-table"
         |  }
         |}""".stripMargin

    val config = ConfigFactory.parseString(SampleConfiguraion)
  }

  "The config for HBase Rest UnitLinks repository" - {
    "can be successfully loaded when valid" in new ValidConfigurationFixture {
      HBaseRestUnitLinksRepositoryConfigLoader.load(config) shouldBe HBaseRestUnitLinksRepositoryConfig("unitlinks-table")
    }

    "should thro error when invalid config is loaded" - {
      "given a missing unit link table name value" in new MissingConfigurationFixture {
        a[ConfigException] should be thrownBy {
          HBaseRestUnitLinksRepositoryConfigLoader.load(config)
        }
      }
    }
  }

}
