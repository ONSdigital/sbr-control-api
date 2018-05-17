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
          optionalString("ruref", reportingUnit.ruref),
          string("ern", reportingUnit.ern.value),
          optionalString("entref", reportingUnit.entref),
          string("name", reportingUnit.name),
          optionalString("tradingStyle", reportingUnit.tradingStyle),
          optionalString("legalStatus", reportingUnit.legalStatus),
          string("address1", reportingUnit.address1),
          optionalString("address2", reportingUnit.address2),
          optionalString("address3", reportingUnit.address3),
          optionalString("address4", reportingUnit.address4),
          optionalString("address5", reportingUnit.address5),
          string("postcode", reportingUnit.postcode),
          string("sic07", reportingUnit.sic07),
          string("employees", reportingUnit.employees),
          string("employment", reportingUnit.employment),
          string("turnover", reportingUnit.turnover),
          string("prn", reportingUnit.prn)
        )
      }
         |}""".stripMargin
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