package uk.gov.ons.sbr.models.reportingunit

import org.scalatest.{ FreeSpec, Matchers }
import play.api.libs.json.Json
import support.JsonString._
import support.sample.SampleReportingUnit

class ReportingUnitSpec extends FreeSpec with Matchers {

  private trait Fixture extends SampleReportingUnit {
    def expectedJsonStrOf(reportingUnit: ReportingUnit): String =
      s"""
         |{${
        withValues(
          string("rurn", reportingUnit.rurn.value),
          optionalString("ruref", reportingUnit.ruref)
        )
      },
         | "enterprise": {${
        withValues(
          string("ern", reportingUnit.enterprise.ern.value),
          optionalString("entref", reportingUnit.enterprise.entref)
        )
      }
         | }, "address": {${
        withValues(
          string("line1", reportingUnit.address.line1),
          optionalString("line2", reportingUnit.address.line2),
          optionalString("line3", reportingUnit.address.line3),
          optionalString("line4", reportingUnit.address.line4),
          optionalString("line5", reportingUnit.address.line5),
          string("postcode", reportingUnit.address.postcode)
        )
      }
         | }, ${
        withValues(
          string("name", reportingUnit.name),
          optionalString("tradingStyle", reportingUnit.tradingStyle),
          string("legalStatus", reportingUnit.legalStatus),
          string("sic07", reportingUnit.sic07),
          int("employees", reportingUnit.employees),
          int("employment", reportingUnit.employment),
          int("turnover", reportingUnit.turnover),
          string("region", reportingUnit.region),
          string("prn", reportingUnit.prn.toString())
        )
      }
      }""".stripMargin
  }

  "A Reporting Unit" - {
    "can be represented as JSON" - {
      "when all fields are defined" in new Fixture {
        Json.toJson(SampleAllValuesReportingUnit) shouldBe Json.parse(expectedJsonStrOf(SampleAllValuesReportingUnit))
      }

      "when only the mandatory fields are defined" in new Fixture {
        Json.toJson(SampleMandatoryValuesReportingUnit) shouldBe Json.parse(expectedJsonStrOf(SampleMandatoryValuesReportingUnit))
      }
    }
  }
}