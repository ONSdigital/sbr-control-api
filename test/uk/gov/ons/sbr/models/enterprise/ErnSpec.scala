package uk.gov.ons.sbr.models.enterprise

import org.scalatest.{ FreeSpec, Matchers }
import play.api.libs.json.Json

class ErnSpec extends FreeSpec with Matchers {
  private val ErnValue = "some-ern"

  "An Ern" - {
    "is represented in JSON as a simple string" in {
      Json.toJson(Ern(ErnValue)) shouldBe Json.parse(s""" "$ErnValue" """)
    }

    "can be reversed" in {
      Ern.reverse(Ern(ErnValue)) shouldBe "nre-emos"
    }
  }
}
