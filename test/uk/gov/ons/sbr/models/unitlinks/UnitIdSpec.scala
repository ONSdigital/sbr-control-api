package uk.gov.ons.sbr.models.unitlinks

import play.api.libs.json.Json
import org.scalatest.{ FreeSpec, Matchers }

class UnitIdSpec extends FreeSpec with Matchers {

  "A UnitId" - {
    "is represented in JSON as a string as" in {
      val unitId = "100097600"
      Json.toJson(UnitId(unitId)) shouldBe Json.parse(s""" "$unitId" """)
    }
  }

}

