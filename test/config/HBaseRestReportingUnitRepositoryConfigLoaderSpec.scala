package config

import com.typesafe.config.{ Config, ConfigException, ConfigFactory }
import org.scalatest.{ FreeSpec, Matchers }
import repository.hbase.reportingunit.HBaseRestReportingUnitRepositoryConfig

class HBaseRestReportingUnitRepositoryConfigLoaderSpec extends FreeSpec with Matchers {

  private trait ValidFixture {
    val SampleConfiguration: String =
      """|db {
         |  hbase-rest {
         |    enterprise.table.name = "enterprise-table"
         |    unit.links.table.name = "unitlinks-table"
         |    localunit.table.name = "localunit-table"
         |    reportingunit.table.name = "reportingunit-table"
         |  }
         |}""".stripMargin
    val config: Config = ConfigFactory.parseString(SampleConfiguration)
  }

  private trait InvalidFixture {
    val SampleConfiguration: String =
      """|db {
        |  hbase-rest {
        |    enterprise.table.name = "enterprise-table"
        |    unit.links.table.name = "unitlinks-table"
        |    localunit.table.name = "localunit-table"
        |    reportingunit.TABLENAME = "reportingunit-table"
        |  }
        |}""".stripMargin
    val config: Config = ConfigFactory.parseString(SampleConfiguration)
  }

  "The config for the HBase REST ReportingUnit repository" - {
    "can be successfully loaded when valid" in new ValidFixture {
      HBaseRestReportingUnitRepositoryConfigLoader.load(config) shouldBe HBaseRestReportingUnitRepositoryConfig("reportingunit-table")
    }

    "will cause an exception to fail fast if the config is invalid" in new InvalidFixture {
      a[ConfigException] should be thrownBy {
        HBaseRestReportingUnitRepositoryConfigLoader.load(config)
      }
    }
  }

}
