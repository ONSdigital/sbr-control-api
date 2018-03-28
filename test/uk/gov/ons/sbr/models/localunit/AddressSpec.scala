package uk.gov.ons.sbr.models.localunit

import org.scalatest.{ FreeSpec, Matchers }
import play.api.libs.json.Json

class AddressSpec extends FreeSpec with Matchers {

  private trait Fixture {
    def expectedJsonStrOf(address: Address): String =
      s"""|{"line1":"${address.line1}",
          | "line2":"${address.line2}",
          | "line3":"${address.line3}",
          | "line4":"${address.line4}",
          | "line5":"${address.line5}",
          | "postcode":"${address.postcode}"}""".stripMargin
  }

  "An Address" - {
    "can be represented as JSON" - {
      "when fields are populated" in new Fixture {
        val anAddress = Address(line1 = "line1-value", line2 = "line2-value", line3 = "line3-value",
          line4 = "line4-value", line5 = "line5-value", postcode = "postcode-value")

        Json.toJson(anAddress) shouldBe Json.parse(expectedJsonStrOf(anAddress))
      }

      // documenting the fact that our JSON will currently include empty fields
      "when fields are empty" in new Fixture {
        val emptyAddress = Address(line1 = "", line2 = "", line3 = "", line4 = "", line5 = "", postcode = "")

        Json.toJson(emptyAddress) shouldBe Json.parse(expectedJsonStrOf(emptyAddress))
      }
    }
  }
}
