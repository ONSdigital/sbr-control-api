package config

import com.typesafe.config.{ ConfigException, ConfigFactory }
import org.scalatest.{ FreeSpec, Matchers }
import repository.hbase.HBaseRestRepositoryConfig

class HBaseRestRepositoryConfigLoaderSpec extends FreeSpec with Matchers {

  private trait Fixture {
    val SampleConfiguration =
      """|db {
         |  hbase-rest {
         |    username = "example-username"
         |    password = "example-password"
         |    timeout = 6000
         |    namespace = "example-namespace"
         |    host = "http://example-hostname"
         |    port = "1234"
         |  }
         |}""".stripMargin
    val config = ConfigFactory.parseString(SampleConfiguration)
  }

  "The config for the HBase REST repository" - {
    "can be successfully loaded when valid" in new Fixture {
      HBaseRestRepositoryConfigLoader.load(config) shouldBe HBaseRestRepositoryConfig(
        protocolWithHostname = "http://example-hostname",
        port = "1234",
        namespace = "example-namespace",
        username = "example-username",
        password = "example-password",
        timeout = 6000L
      )
    }

    "cannot be loaded" - {
      "when a required key is missing" in new Fixture {
        val mandatoryKeys = Seq("username", "password", "timeout", "namespace", "host", "port")
        mandatoryKeys.foreach { key =>
          withClue(s"with missing key $key") {
            val config = ConfigFactory.parseString(SampleConfiguration.replaceFirst(key, "missing"))
            a[ConfigException] should be thrownBy {
              HBaseRestRepositoryConfigLoader.load(config)
            }
          }
        }
      }

      "when timeout is non-numeric" in new Fixture {
        override val config = ConfigFactory.parseString(SampleConfiguration.replaceFirst("timeout = 6000", """timeout="foo""""))
        a[ConfigException] should be thrownBy {
          HBaseRestRepositoryConfigLoader.load(config)
        }
      }
    }
  }
}
