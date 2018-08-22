package uk.gov.ons.sbr.models.localunit

import org.scalatest.{ FreeSpec, Matchers }
import play.api.libs.json.Json

class LurnSpec extends FreeSpec with Matchers {
  "A Lurn" - {
    "is represented in JSON as a simple string" in {
      val lurnValue = "some-lurn"

      Json.toJson(Lurn(lurnValue)) shouldBe Json.parse(s""" "$lurnValue" """)
    }
  }
}
