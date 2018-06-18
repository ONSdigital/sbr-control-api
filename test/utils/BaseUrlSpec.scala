package utils.url

import org.scalatest.{ FreeSpec, Matchers }
import utils.BaseUrl
import utils.BaseUrl.asUrlString

class BaseUrlSpec extends FreeSpec with Matchers {
  "A BaseUrl" - {
    "can be represented as a URL string" in {
      asUrlString(BaseUrl("protocol", "hostname", port = 1234, None)) shouldBe "protocol://hostname:1234/"
    }
  }
}
