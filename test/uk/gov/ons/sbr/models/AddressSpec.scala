package uk.gov.ons.sbr.models

import play.api.libs.json.Json
import org.scalatest.{ FreeSpec, Matchers }

import support.JsonString
import support.JsonString.{ optionalString, string }

class AddressSpec extends FreeSpec with Matchers {

  private trait Fixture {
    def expectedJsonStrOf(address: Address): String =
      JsonString.withObject(
        string("line1", address.line1),
        optionalString("line2", address.line2),
        optionalString("line3", address.line3),
        optionalString("line4", address.line4),
        optionalString("line5", address.line5),
        string("postcode", address.postcode)
      )
  }

  "An Address" - {
    "can be represented as JSON" - {
      "when all fields are defined" in new Fixture {
        val anAddress = Address(line1 = "line1-value", line2 = Some("line2-value"), line3 = Some("line3-value"),
          line4 = Some("line4-value"), line5 = Some("line5-value"), postcode = "postcode-value")

        Json.toJson(anAddress) shouldBe Json.parse(expectedJsonStrOf(anAddress))
      }

      "when only mandatory fields are defined" in new Fixture {
        val anAddress = Address(line1 = "line1-value", line2 = None, line3 = None, line4 = None, line5 = None,
          postcode = "postcode-value")

        Json.toJson(anAddress) shouldBe Json.parse(expectedJsonStrOf(anAddress))
      }
    }
  }
}
