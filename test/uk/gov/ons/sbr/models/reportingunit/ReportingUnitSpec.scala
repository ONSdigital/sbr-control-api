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