package uk.gov.ons.sbr.models.enterprise

import org.scalatest.{ FreeSpec, Matchers }
import play.api.libs.json.Json
import support.JsonString.{ optionalInt, withObject }
import support.sample.SampleEnterpriseUnit

class ImputedSpec extends FreeSpec with Matchers {

  private trait Fixture extends SampleEnterpriseUnit {
    def expectedJsonStrOf(imputed: Imputed): String =
      withObject(
        optionalInt("employees", imputed.employees),
        optionalInt("turnover", imputed.turnover)
      )
  }

  "Imputed Enterprise Variables" - {
    "can be represented as JSON" - {
      "when all fields are defined" in new Fixture {
        Json.toJson(SampleImputedWithAllFields) shouldBe Json.parse(expectedJsonStrOf(SampleImputedWithAllFields))
      }

      "when turnover is not defined" in new Fixture {
        val imputedFieldsMissingTurnover = SampleImputedWithAllFields.copy(turnover = None)

        Json.toJson(imputedFieldsMissingTurnover) shouldBe Json.parse(expectedJsonStrOf(imputedFieldsMissingTurnover))
      }

      "when employees is not defined" in new Fixture {
        val imputedFieldsMissingEmployees = SampleImputedWithAllFields.copy(employees = None)

        Json.toJson(imputedFieldsMissingEmployees) shouldBe Json.parse(expectedJsonStrOf(imputedFieldsMissingEmployees))
      }
    }
  }
}
