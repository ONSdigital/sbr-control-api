package uk.gov.ons.sbr.models.unitlinks

import play.api.libs.json.Json
import org.scalatest.{ FreeSpec, Matchers }

import uk.gov.ons.sbr.models.Period

import support.JsonString._
import support.sample.SampleUnitLinks

class UnitLinksSpec extends FreeSpec with Matchers {

  private trait Fixture extends SampleUnitLinks {

    def expectedJsonStrOf(unitLinks: UnitLinks) =
      s"""
         |{${
        withValues(
          string(name = "id", value = unitLinks.id.value),
          string(name = "period", value = Period.asString(unitLinks.period)),
          optionalMap(name = "parents", optValue = unitLinks.parents)(convertKey = UnitType.toAcronym, convertValue = unitIdAsString),
          optionalMap(name = "children", optValue = unitLinks.children)(convertKey = unitIdAsString, convertValue = UnitType.toAcronym),
          string(name = "unitType", value = UnitType.toAcronym(unitLinks.unitType))
        )
      }
        |}""".stripMargin
  }

  "A UnitLinks" - {
    "can be represented as JSON when " - {
      "all mandatory field are provided" in new Fixture {
        Json.toJson(SampleUnitLinksWithAllFields) shouldBe Json.parse(expectedJsonStrOf(SampleUnitLinksWithAllFields))
      }

      "a optional children field only is given" in new Fixture {
        val childrenFieldMapOnly = aUnitLinksSample(parents = None)
        Json.toJson(childrenFieldMapOnly) shouldBe Json.parse(expectedJsonStrOf(childrenFieldMapOnly))
      }

      "a optional parents field only is given" in new Fixture {
        val parentsFieldMapOnly = aUnitLinksSample(children = None)
        Json.toJson(parentsFieldMapOnly) shouldBe Json.parse(expectedJsonStrOf(parentsFieldMapOnly))
      }
    }
  }

}

