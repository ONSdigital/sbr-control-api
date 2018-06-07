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
          optionalString("crn", legalUnit.crn),
          string("name", legalUnit.name),
          string("legalStatus", legalUnit.legalStatus),
          string("tradingStatus", legalUnit.tradingStatus),
          optionalString("tradingstyle", legalUnit.tradingstyle),
          string("sic07", legalUnit.sic07),
          optionalInt("turnover", legalUnit.turnover),
          optionalInt("jobs", legalUnit.jobs)
        )
      },
         | "enterprise": {${
        withValues(
          string("ern", legalUnit.enterprise.ern.value),
          optionalString("entref", legalUnit.enterprise.entref)
        )
      }
         | },
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
  }
}
