package utils.url

import org.scalatest.{ FreeSpec, Matchers }
import utils.BaseUrl
import utils.BaseUrl.asUrlString

class BaseUrlSpec extends FreeSpec with Matchers {
  "A BaseUrl" - {
    "can be represented as a URL string" - {
      "containing a null or empty string prefix" in {
        asUrlString(BaseUrl(protocol = "http", host = "hostname", port = 1234, prefix = None)) shouldBe "http://hostname:1234"
      }
      "containing a prefix" in {
        asUrlString(BaseUrl(protocol = "http", host = "hostname", port = 1234, prefix = Some("HBase"))) shouldBe "http://hostname:1234/HBase"
      }
    }
  }
}
