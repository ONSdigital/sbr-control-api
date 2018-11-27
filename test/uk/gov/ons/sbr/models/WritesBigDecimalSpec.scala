package uk.gov.ons.sbr.models

import org.scalatest.{ FreeSpec, Matchers }
import play.api.libs.json.JsString

class WritesBigDecimalSpec extends FreeSpec with Matchers {
  "A Big Decimal" - {
    "can be represented as a JSON string" - {
      "in order to retain precision" in {
        WritesBigDecimal.writes(BigDecimal("2.7182818284")) shouldBe JsString("2.7182818284")
      }

      /*
       * This covers very small PRN values.
       * We do not want such values to be represented in scientific notation - which is the default behaviour of
       * BigDecimal.toString.
       * See https://docs.oracle.com/javase/8/docs/api/java/math/BigDecimal.html#toString--
       */
      "in non-scientific notation when very small" in {
        WritesBigDecimal.writes(BigDecimal("0.000000004")) shouldBe JsString("0.000000004")
      }
    }
  }
}
