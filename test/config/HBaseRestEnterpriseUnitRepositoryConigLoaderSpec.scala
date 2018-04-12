package config

import org.scalatest.{ FreeSpec, Matchers }
import com.typesafe.config.{ Config, ConfigFactory }

import repository.hbase.unit.enterprise.HBaseRestEnterpriseUnitRepositoryConfig

class HBaseRestEnterpriseUnitRepositoryConigLoaderSpec extends FreeSpec with Matchers {

  private trait Fixture {
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

  "The config for the HBase REST LocalUnit repository" - {
    "can be successfully loaded when valid" in new Fixture {
      HBaseRestEnterpriseUnitRepositoryConfigLoader.load(config) shouldBe HBaseRestEnterpriseUnitRepositoryConfig("enterprise-table")
    }
  }

}
