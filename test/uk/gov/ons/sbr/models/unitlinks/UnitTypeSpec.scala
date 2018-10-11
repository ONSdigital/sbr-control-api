package uk.gov.ons.sbr.models.unitlinks

import play.api.libs.json.JsString
import org.scalatest.{ FreeSpec, Matchers }

class UnitTypeSpec extends FreeSpec with Matchers {

  "A UnitType" - {
    "can be represented as an acronym" - {
      "when an Companies House" in {
        UnitType.toAcronym(UnitType.CompaniesHouse) shouldBe "CH"
      }

      "when an Value-added Tax (VAT)" in {
        UnitType.toAcronym(UnitType.ValueAddedTax) shouldBe "VAT"
      }

      "when an Pay-as-you-earn Tax (PAYE)" in {
        UnitType.toAcronym(UnitType.PayAsYouEarn) shouldBe "PAYE"
      }

      "when an Enterprise" in {
        UnitType.toAcronym(UnitType.Enterprise) shouldBe "ENT"
      }

      "when an Legal Unit" in {
        UnitType.toAcronym(UnitType.LegalUnit) shouldBe "LEU"
      }

      "when an Local Unit" in {
        UnitType.toAcronym(UnitType.LocalUnit) shouldBe "LOU"
      }

      "when an Reporting Unit" in {
        UnitType.toAcronym(UnitType.ReportingUnit) shouldBe "REU"
      }
    }

    "can be derived from a defined UnitType acronym" - {
      "when an Companies House acronym" in {
        UnitType.fromAcronym("CH") shouldBe UnitType.CompaniesHouse
      }

      "when an Value-added Tax (VAT) acronym" in {
        UnitType.fromAcronym("VAT") shouldBe UnitType.ValueAddedTax
      }

      "when an Pay-as-you-earn Tax (Paye) acronym" in {
        UnitType.fromAcronym("PAYE") shouldBe UnitType.PayAsYouEarn
      }

      "when an Enterprise acronym" in {
        UnitType.fromAcronym("ENT") shouldBe UnitType.Enterprise
      }

      "when an Legal Unit acronym" in {
        UnitType.fromAcronym("LEU") shouldBe UnitType.LegalUnit
      }

      "when an Local Unit acronym" in {
        UnitType.fromAcronym("LOU") shouldBe UnitType.LocalUnit
      }

      "when an Reporting Unit acronym" in {
        UnitType.fromAcronym("REU") shouldBe UnitType.ReportingUnit
      }
    }

    "JSON representation" - {
      "can be written when a UnitType as string is given" - {
        "with a Companies House" in {
          UnitType.writes.writes(UnitType.CompaniesHouse) shouldBe JsString("CH")
        }

        "with a Value-added Tax (VAT)" in {
          UnitType.writes.writes(UnitType.ValueAddedTax) shouldBe JsString("VAT")
        }

        "with a Pay-as-you-earn Tax (Paye)" in {
          UnitType.writes.writes(UnitType.PayAsYouEarn) shouldBe JsString("PAYE")
        }

        "with a Enterprise" in {
          UnitType.writes.writes(UnitType.Enterprise) shouldBe JsString("ENT")
        }

        "with a Legal Unit" in {
          UnitType.writes.writes(UnitType.LegalUnit) shouldBe JsString("LEU")
        }

        "with a Local Unit" in {
          UnitType.writes.writes(UnitType.LocalUnit) shouldBe JsString("LOU")
        }

        "with a Reporting Unit" in {
          UnitType.writes.writes(UnitType.ReportingUnit) shouldBe JsString("REU")
        }
      }
    }

  }

}
