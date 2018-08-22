package uk.gov.ons.sbr.models.legalUnit

import org.scalatest.{ FreeSpec, Matchers }
import play.api.libs.json.Json
import uk.gov.ons.sbr.models.legalunit.Ubrn

class UbrnSpec extends FreeSpec with Matchers {
  "A UBRN" - {
    "is represented in JSON as a simple string" in {
      val ubrnValue = "some-ubrn"

      Json.toJson(Ubrn(ubrnValue)) shouldBe Json.parse(s""""$ubrnValue"""")
    }
  }
}
