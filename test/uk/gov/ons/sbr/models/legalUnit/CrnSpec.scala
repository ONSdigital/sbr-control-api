package uk.gov.ons.sbr.models.legalUnit

import org.scalatest.{ FreeSpec, Matchers }
import play.api.libs.json.Json
import uk.gov.ons.sbr.models.legalunit.Crn

class CrnSpec extends FreeSpec with Matchers {
  "A CRN" - {
    "is represented in JSON as a simple string" in {
      val crnValue = "some-crn"

      Json.toJson(Crn(crnValue)) shouldBe Json.parse(s""""$crnValue"""")
    }
  }
}
