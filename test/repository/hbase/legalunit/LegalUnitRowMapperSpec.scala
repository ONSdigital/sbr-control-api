package repository.hbase.legalunit

import com.typesafe.scalalogging.LazyLogging
import org.scalatest.{ FreeSpec, Matchers }
import repository.RestRepository.Row
import repository.hbase.legalunit.LegalUnitColumns._
import uk.gov.ons.sbr.models.enterprise.{ EnterpriseLink, Ern }
import uk.gov.ons.sbr.models.legalunit.{ LegalUnit, UBRN }
import uk.gov.ons.sbr.models.Address

class LegalUnitRowMapperSpec extends FreeSpec with Matchers with LazyLogging {

  private trait Fixture {
    val ubrnValue = "ab"
    val ernValue = "ef"
    val entrefValue = "gh"
    val nameValue = "ij"
    val tradingstyleValue = "kl"
    val address1Value = "mn"
    val address2Value = "op"
    val address3Value = "qr"
    val address4Value = "st"
    val address5Value = "uv"
    val postcodeValue = "wx"
    val sic07Value = "yz"
    val crnValue = "12"
    val jobsValue = "34"
    val legalStatusValue = "56"
    val tradingStatusValue = "78"
    val turnoverValue = "90"

    val allVariables = Map(ubrn -> ubrnValue, ern -> ernValue, entref -> entrefValue, name -> nameValue, tradingstyle -> tradingstyleValue,
      address1 -> address1Value, address2 -> address2Value, address3 -> address3Value, address4 -> address4Value, address5 -> address5Value,
      postcode -> postcodeValue, sic07 -> sic07Value, crn -> crnValue, jobs -> jobsValue, legalStatus -> legalStatusValue,
      tradingStatus -> tradingStatusValue, turnover -> turnoverValue)
    private val optionalColumns = Seq(entref, crn, tradingstyle, turnover, jobs, address2, address3, address4, address5)
    val mandatoryVariables: Map[String, String] = allVariables -- optionalColumns
    val UnusedRowKey = ""
  }
  "A LegalUnit row mapper" - {
    "can create a LegalUnit when all possible variables are defined" in new Fixture {
      LegalUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = allVariables)) shouldBe Some(LegalUnit(
        UBRN(ubrnValue),
        crn = Some(crnValue), name = nameValue, legalStatus = legalStatusValue, tradingStatus = tradingStatusValue,
        tradingstyle = Some(tradingstyleValue), sic07 = sic07Value, turnover = Some(turnoverValue.toInt), jobs = Some(jobsValue.toInt),
        enterprise = EnterpriseLink(Ern(ernValue), entref = Some(entrefValue)), address = Address(
          line1 = address1Value,
          line2 = Some(address2Value), line3 = Some(address3Value), line4 = Some(address4Value), line5 = Some(address5Value),
          postcode = postcodeValue
        )
      ))
    }
    "can create a LegalUnit when only the mandatory variables are defined" in new Fixture {
      LegalUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = mandatoryVariables)) shouldBe Some(LegalUnit(
        UBRN(ubrnValue), crn = None, name = nameValue, legalStatus = legalStatusValue, tradingStatus = tradingStatusValue,
        tradingstyle = None, sic07 = sic07Value, turnover = None, jobs = None, enterprise = EnterpriseLink(Ern(ernValue), entref = None),
        address = Address(line1 = address1Value, line2 = None, line3 = None, line4 = None, line5 = None, postcode = postcodeValue)
      ))
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
      "the value of jobs is non-numeric" in new Fixture {
        LegalUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = allVariables.updated(jobs, "invalid_int"))) shouldBe None
      }

      "the value of jobs is not an integral value" in new Fixture {
        LegalUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = allVariables.updated(jobs, "3.14159"))) shouldBe None
      }
    }
  }
}
