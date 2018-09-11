package uk.gov.ons.sbr.models.unitlinks

import play.api.libs.json._
import org.scalatest.{ FreeSpec, Matchers }

class UnitIdSpec extends FreeSpec with Matchers {

  private trait Fixture {
    val UnitRef = "100097600"
  }

  "A UnitId" - {
    "can be written as a JSON string" in new Fixture {
      Json.toJson(UnitId(UnitRef)) shouldBe JsString(UnitRef)
    }

    "can be read from a JSON string" in new Fixture {
      val unitIdJson = JsString(UnitRef)

      unitIdJson.validate[UnitId] shouldBe JsSuccess(UnitId(UnitRef))
    }

    "cannot be read from a non-string JSON value" in new Fixture {
      val badUnitIdJson = JsNumber(42)

      badUnitIdJson.validate[UnitId] shouldBe a[JsError]
    }
  }
}

