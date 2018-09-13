package uk.gov.ons.sbr.models.localunit

import org.scalatest.{ FreeSpec, Matchers }
import play.api.libs.json.Json
import support.JsonString.{ int, optionalString, string, withValues }
import support.sample.SampleLocalUnit

class LocalUnitSpec extends FreeSpec with Matchers {

  private trait Fixture extends SampleLocalUnit {
    def expectedJsonStrOf(localUnit: LocalUnit): String =
      s"""
         |{${
        withValues(
          string("lurn", localUnit.lurn.value),
          optionalString("luref", localUnit.luref)
        )
      },
         | "enterprise": {${
        withValues(
          string("ern", localUnit.enterprise.ern.value),
          optionalString("entref", localUnit.enterprise.entref)
        )
      }
         | },
         | "reportingUnit": {${
        withValues(
          string("rurn", localUnit.reportingUnit.rurn.value),
          optionalString("ruref", localUnit.reportingUnit.ruref)
        )
      }
         | },
         |  ${
        withValues(
          string("name", localUnit.name),
          optionalString("tradingStyle", localUnit.tradingStyle),
          string("sic07", localUnit.sic07),
          int("employees", localUnit.employees),
          int("employment", localUnit.employment),
          string("region", localUnit.region),
          string("prn", localUnit.prn.toString())
        )
      },
         | "address": {${
        withValues(
          string("line1", localUnit.address.line1),
          optionalString("line2", localUnit.address.line2),
          optionalString("line3", localUnit.address.line3),
          optionalString("line4", localUnit.address.line4),
          optionalString("line5", localUnit.address.line5),
          string("postcode", localUnit.address.postcode)
        )
      }}}""".stripMargin
  }

  "A LocalUnit" - {
    "can be represented as JSON" - {
      "when all fields are defined" in new Fixture {
        Json.toJson(SampleAllValuesLocalUnit) shouldBe Json.parse(expectedJsonStrOf(SampleAllValuesLocalUnit))
      }

      "when only the mandatory fields are defined" in new Fixture {
        Json.toJson(SampleMandatoryValuesLocalUnit) shouldBe Json.parse(expectedJsonStrOf(SampleMandatoryValuesLocalUnit))
      }
    }
  }
}
