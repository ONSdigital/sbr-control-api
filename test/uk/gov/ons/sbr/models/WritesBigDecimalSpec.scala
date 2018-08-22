package uk.gov.ons.sbr.models

import org.scalatest.{ FreeSpec, Matchers }
import play.api.libs.json.JsString

class WritesBigDecimalSpec extends FreeSpec with Matchers {

  "A Big Decimal" - {
    "can be represented as a JSON string in order to retain precision" in {
      WritesBigDecimal.writes(BigDecimal("2.7182818284")) shouldBe JsString("2.7182818284")
    }
  }
}
