package uk.gov.ons.sbr.models.enterprise

import play.api.libs.json.Json
import org.scalatest.{ FreeSpec, Matchers, OptionValues }

import support.sample.SampleEnterpriseUnit
import support.JsonString._

class EnterpriseSpec extends FreeSpec with Matchers with OptionValues {

  private trait Fixture extends SampleEnterpriseUnit {

    private def writeAsJsonTurnover(turnover: Turnover) =
      s"""
         |, "turnover":{ ${
        withValues(
          optionalInt(name = "containedTurnover", optValue = turnover.containedTurnover),
          optionalInt(name = "standardTurnover", optValue = turnover.standardTurnover),
          optionalInt(name = "groupTurnover", optValue = turnover.groupTurnover),
          optionalInt(name = "apportionedTurnover", optValue = turnover.apportionedTurnover),
          optionalInt(name = "enterpriseTurnover", optValue = turnover.enterpriseTurnover)
        )
      }}
      """

    def expectedJsonOutput(ent: Enterprise): String =
      s"""
         |{${
        withValues(
          string(name = "ern", value = ent.ern.value),
          optionalString(name = "entref", optValue = ent.entref),
          string(name = "name", value = ent.name),
          optionalString(name = "tradingStyle", optValue = ent.tradingStyle)
        )
      },
         |"address": {${
        withValues(
          string(name = "line1", value = ent.address.line1),
          optionalString(name = "line2", optValue = ent.address.line2),
          optionalString(name = "line3", optValue = ent.address.line3),
          optionalString(name = "line4", optValue = ent.address.line4),
          optionalString(name = "line5", optValue = ent.address.line5),
          string(name = "postcode", value = ent.address.postcode)
        )
      }},
        ${
        withValues(
          string(name = "sic07", value = ent.sic07),
          string(name = "legalStatus", value = ent.legalStatus),
          optionalInt(name = "employees", optValue = ent.employees),
          optionalInt(name = "jobs", optValue = ent.jobs)
        )
      }
        ${
        ent.turnover.fold("") { turnover =>
          writeAsJsonTurnover(turnover = turnover)
        }
      }}""".stripMargin
  }

  "A enterprise" - {
    "should have a JSON representation" - {
      "when all field are included - both mandatory and optional" in new Fixture {
        Json.toJson(SampleEnterpriseWithAllFields) shouldBe Json.parse(expectedJsonOutput(SampleEnterpriseWithAllFields))
      }

      "when only mandatory field are given - i.e. excluding optional" in new Fixture {
        Json.toJson(SampleEnterpriseWithNoOptionalFields) shouldBe Json.parse(expectedJsonOutput(SampleEnterpriseWithNoOptionalFields))
      }
    }
  }
}

