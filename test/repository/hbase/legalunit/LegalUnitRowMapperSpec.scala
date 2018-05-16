package repository.hbase.legalunit

import org.scalatest.{ FreeSpec, Matchers }
import repository.RestRepository.Row
import repository.hbase.legalunit.LegalUnitColumns._
import uk.gov.ons.sbr.models.enterprise.{ EnterpriseLink, Ern }
import uk.gov.ons.sbr.models.legalunit.{ LegalUnit, UBRN }

class LegalUnitRowMapperSpec extends FreeSpec with Matchers {

  private trait Fixture {
    val uBRNValue = "ab"
    val UBRNrefValue = "cd"
    val ernValue = "ef"
    val entrefValue = "gh"

    val allVariables = Map(uBRN -> uBRNValue, UBRNref -> UBRNrefValue, ern -> ernValue, entref -> entrefValue)
    private val optionalColumns = Seq(UBRNref, entref)
    val mandatoryVariables: Map[String, String] = allVariables -- optionalColumns

    val UnusedRowKey = ""
  }

  "A LegalUnit row mapper" - {
    "can create a LegalUnit when all possible variables are defined" in new Fixture {
      LegalUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = allVariables)) shouldBe Some(LegalUnit(UBRN(uBRNValue), UBRNref = Some(UBRNrefValue),
        enterprise = EnterpriseLink(Ern(ernValue), entref = Some(entrefValue))))
    }

    "can create a LegalUnit when only the mandatory variables are defined" in new Fixture {
      LegalUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = mandatoryVariables)) shouldBe Some(LegalUnit(UBRN(uBRNValue), UBRNref = None,
        enterprise = EnterpriseLink(Ern(ernValue), entref = None)))
    }

    "cannot create a LegalUnit when" - {
      "a mandatory variable is missing" in new Fixture {
        val mandatoryColumns = mandatoryVariables.keys
        mandatoryColumns.foreach { column =>
          withClue(s"with missing column [$column]") {
            LegalUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = mandatoryVariables - column)) shouldBe None
          }
        }
      }
    }
  }
}
