package uk.gov.ons.sbr.models.reportingunit

import org.scalatest.{ FreeSpec, Matchers }
import play.api.libs.json.Json

class RurnSpec extends FreeSpec with Matchers {
  "A Rurn" - {
    "is represented in JSON as a simple string" in {
      val rurnValue = "some-rurn"

      Json.toJson(Rurn(rurnValue)) shouldBe Json.parse(s""" "$rurnValue" """)
    }
  }
}
