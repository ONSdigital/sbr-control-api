package uk.gov.ons.sbr.models.reportingunit

import play.api.libs.json.Json
import org.scalatest.{FreeSpec, Matchers}

import repository.hbase.reportingunit.ReportingUnitColumns.{ruref, rurn}
import support.JsonString
import support.JsonString.{optionalString, string}
import support.sample.SampleReportingUnit


class ReportingUnitLinkSpec extends FreeSpec with Matchers{
  private trait Fixture extends SampleReportingUnit{
    def expectedJsonStrOf(ru: ReportingUnitLink): String = {
      JsonString.withObject(
        string(rurn, ru.rurn.value),
        optionalString(ruref, ru.ruref)
      )
    }
  }

  "A ReportingUnitLinks" - {
    "can be represented as json" - {
      "when all fields are provided" in new Fixture {
        val aReportingUnitLink = ReportingUnitLink(rurn = Rurn("33000000001"), ruref = Some("ruref-124"))
        Json.toJson(aReportingUnitLink) shouldBe Json.parse(expectedJsonStrOf(aReportingUnitLink))
      }

      "when only mandatory fields" in new Fixture {
        val aReportingUnitLink = ReportingUnitLink(rurn = Rurn("33000000001"), ruref = None)
        Json.toJson(aReportingUnitLink) shouldBe Json.parse(expectedJsonStrOf(aReportingUnitLink))
      }
    }
  }
}
