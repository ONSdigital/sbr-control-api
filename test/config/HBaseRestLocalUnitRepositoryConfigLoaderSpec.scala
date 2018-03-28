package config

import com.typesafe.config.ConfigFactory
import org.scalatest.{ FreeSpec, Matchers }
import repository.hbase.HBaseRestLocalUnitRepositoryConfig

class HBaseRestLocalUnitRepositoryConfigLoaderSpec extends FreeSpec with Matchers {

  private trait Fixture {
    val SampleConfiguration =
      """|db {
         |  hbase-rest {
         |    enterprise.table.name = "enterprise-table"
         |    unit.links.table.name = "unitlinks-table"
         |    localunit.table.name = "localunit-table"
         |  }
         |}""".stripMargin
    val config = ConfigFactory.parseString(SampleConfiguration)
  }

  "The config for the HBase REST LocalUnit repository" - {
    "can be successfully loaded when valid" in new Fixture {
      HBaseRestLocalUnitRepositoryConfigLoader.load(config) shouldBe HBaseRestLocalUnitRepositoryConfig("localunit-table")
    }
  }
}
