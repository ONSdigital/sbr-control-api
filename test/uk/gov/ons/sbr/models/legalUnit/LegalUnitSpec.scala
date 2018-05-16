package uk.gov.ons.sbr.models.legalUnit

import org.scalatest.{ FreeSpec, Matchers }
import play.api.libs.json.Json
import support.JsonString.{ int, optionalString, string, withValues }
import support.sample.SampleLegalUnit
import uk.gov.ons.sbr.models.legalunit.LegalUnit

class LegalUnitSpec extends FreeSpec with Matchers {

  private trait Fixture extends SampleLegalUnit {
    def expectedJsonStrOf(legalUnit: LegalUnit): String =
      s"""
         |{${
        withValues(
          string("uBRN", legalUnit.uBRN.value),
          optionalString("UBRNref", legalUnit.UBRNref)
        )
      },
         | "enterprise": {${
        withValues(
          string("ern", legalUnit.enterprise.ern.value),
          optionalString("entref", legalUnit.enterprise.entref)
        )
      }
         | }
         |}""".stripMargin
  }

  "A LegalUnit" - {
    "can be represented as JSON" - {
      "when all fields are defined" in new Fixture {
        println(expectedJsonStrOf(SampleAllValuesLegalUnit))
        Json.toJson(SampleAllValuesLegalUnit) shouldBe Json.parse(expectedJsonStrOf(SampleAllValuesLegalUnit))
      }

      "when only the mandatory fields are defined" in new Fixture {
        Json.toJson(SampleMandatoryValuesLegalUnit) shouldBe Json.parse(expectedJsonStrOf(SampleMandatoryValuesLegalUnit))
      }
    }

    "A LocalUnit" - {
      "can be represented as JSON" - {
        "when all fields are defined" in new Fixture {
          Json.toJson(SampleAllValuesLegalUnit) shouldBe Json.parse(expectedJsonStrOf(SampleAllValuesLegalUnit))
        }

        "when only the mandatory fields are defined" in new Fixture {
          Json.toJson(SampleMandatoryValuesLegalUnit) shouldBe Json.parse(expectedJsonStrOf(SampleMandatoryValuesLegalUnit))
        }
      }
    }
  }
}
