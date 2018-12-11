package uk.gov.ons.sbr.models.enterprise

import org.scalatest.{ FreeSpec, Matchers, OptionValues }
import play.api.libs.json.Json
import support.JsonString._
import support.sample.SampleEnterpriseUnit

class EnterpriseSpec extends FreeSpec with Matchers with OptionValues {

  private trait Fixture extends SampleEnterpriseUnit {

    private def writeTurnoverAsJson(turnover: Turnover): String =
      ", " + withObject("turnover", values =
        optionalInt(name = "containedTurnover", optValue = turnover.containedTurnover),
        optionalInt(name = "standardTurnover", optValue = turnover.standardTurnover),
        optionalInt(name = "groupTurnover", optValue = turnover.groupTurnover),
        optionalInt(name = "apportionedTurnover", optValue = turnover.apportionedTurnover),
        optionalInt(name = "enterpriseTurnover", optValue = turnover.enterpriseTurnover))

    private def writeImputedAsJson(imputed: Imputed): String =
      ", " + withObject("imputed", values =
        optionalInt("employees", imputed.employees),
        optionalInt("turnover", imputed.turnover))

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
          string(name = "region", value = ent.region),
          string(name = "sic07", value = ent.sic07),
          string(name = "legalStatus", value = ent.legalStatus),
          optionalInt(name = "employees", optValue = ent.employees),
          optionalInt(name = "jobs", optValue = ent.jobs),
          int(name = "workingProprietors", value = ent.workingProprietors),
          int(name = "employment", value = ent.employment),
          string(name = "prn", value = ent.prn.bigDecimal.toPlainString)
        )
      }
        ${
        ent.turnover.fold("") {
          writeTurnoverAsJson
        }
      }
        ${
        ent.imputed.fold("") {
          writeImputedAsJson
        }
      }
        }""".stripMargin
  }

  "A enterprise" - {
    "should have a JSON representation" - {
      "when all field are included - both mandatory and optional" in new Fixture {
        Json.toJson(SampleEnterpriseWithAllFields) shouldBe Json.parse(expectedJsonOutput(SampleEnterpriseWithAllFields))
      }

      /*
       * In this scenario - the optional sub-objects for turnover & imputed will be missing entirely
       */
      "when only mandatory field are given - i.e. excluding optional" in new Fixture {
        Json.toJson(SampleEnterpriseWithNoOptionalFields) shouldBe Json.parse(expectedJsonOutput(SampleEnterpriseWithNoOptionalFields))
      }

      "when turnover is partially populated" in new Fixture {
        val sampleEnterpriseWithPartialTurnover = SampleEnterpriseWithAllFields.copy(
          turnover = Some(SampleTurnoverWithAllFields.copy(groupTurnover = None, apportionedTurnover = None))
        )

        Json.toJson(sampleEnterpriseWithPartialTurnover) shouldBe Json.parse(expectedJsonOutput(sampleEnterpriseWithPartialTurnover))
      }

      "when the imputed variables are partially populated" in new Fixture {
        val sampleEnterpriseWithPartialImputation = SampleEnterpriseWithAllFields.copy(
          imputed = Some(SampleImputedWithAllFields.copy(employees = None))
        )

        Json.toJson(sampleEnterpriseWithPartialImputation) shouldBe Json.parse(expectedJsonOutput(sampleEnterpriseWithPartialImputation))
      }

      // representation should contain the full value in non-scientific format
      "when the prn is very small" in new Fixture {
        val sampleEnterpriseWithVerySmallPrn = SampleEnterpriseWithAllFields.copy(prn = BigDecimal("0.000000004"))

        Json.toJson(sampleEnterpriseWithVerySmallPrn) shouldBe Json.parse(expectedJsonOutput(sampleEnterpriseWithVerySmallPrn))
      }
    }
  }
}

