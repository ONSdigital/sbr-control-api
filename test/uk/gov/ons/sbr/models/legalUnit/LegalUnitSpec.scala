package uk.gov.ons.sbr.models.legalUnit

import org.scalatest.{ FreeSpec, Matchers }
import play.api.libs.json.Json
import support.JsonString.{ optionalInt, optionalString, string, withValues }
import support.sample.SampleLegalUnit
import uk.gov.ons.sbr.models.legalunit.LegalUnit

class LegalUnitSpec extends FreeSpec with Matchers {

  private trait Fixture extends SampleLegalUnit {
    def expectedJsonStrOf(legalUnit: LegalUnit): String =
      s"""
         |{${
        withValues(
          string("ubrn", legalUnit.ubrn.value),
          string("name", legalUnit.name),
          optionalString("tradingStyle", legalUnit.tradingStyle)
        )
      },
         |  "address": {${
        withValues(
          string("line1", legalUnit.address.line1),
          optionalString("line2", legalUnit.address.line2),
          optionalString("line3", legalUnit.address.line3),
          optionalString("line4", legalUnit.address.line4),
          optionalString("line5", legalUnit.address.line5),
          string("postcode", legalUnit.address.postcode)
        )
      }
         | },
         | ${
        withValues(
          string("legalStatus", legalUnit.legalStatus),
          optionalString("tradingStatus", legalUnit.tradingStatus),
          string("sic07", legalUnit.sic07),
          optionalInt("payeJobs", legalUnit.payeJobs),
          optionalInt("turnover", legalUnit.turnover),
          string("birthDate", legalUnit.birthDate),
          optionalString("deathDate", legalUnit.deathDate),
          optionalString("deathCode", legalUnit.deathCode),
          optionalString("crn", legalUnit.crn.map(_.value)),
          optionalString("uprn", legalUnit.uprn.map(_.value))
        )
      }
         |}""".stripMargin
  }

  "A LegalUnit" - {
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
