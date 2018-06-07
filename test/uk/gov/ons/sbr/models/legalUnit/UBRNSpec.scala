package uk.gov.ons.sbr.models.legalUnit

import org.scalatest.{ FreeSpec, Matchers }
import play.api.libs.json.Json
import uk.gov.ons.sbr.models.legalunit.UBRN

class UBRNSpec extends FreeSpec with Matchers {
  "A UBRN" - {
    "is represented in JSON as a simple string" in {
      val uBRNValue = "some-lurn"

      Json.toJson(UBRN(uBRNValue)) shouldBe Json.parse(s""" "$uBRNValue" """)
    }
  }
}
