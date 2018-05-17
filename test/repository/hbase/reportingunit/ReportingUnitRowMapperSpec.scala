package repository.hbase.reportingunit

import org.scalatest.{ FreeSpec, Matchers }
import repository.hbase.reportingunit.ReportingUnitColumns._
import support.sample.SampleReportingUnit
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.reportingunit.{ ReportingUnit, Rurn }

class ReportingUnitRowMapperSpec extends FreeSpec with Matchers with SampleReportingUnit {

  private trait Fixture {
    val entrefValue = "ds"
    val rurnValue = "ab"
    val rurefValue = "cd"
    val ernValue = "cv"
    val nameValue = "fe"
    val address1Value = "ey"
    val address2Value = "ey"
    val address3Value = "ey"
    val address4Value = "ey"
    val address5Value = "ey"
    val postCodeValue = "qr"
    val sic07Value = "qg"
    val employeesValue = "1"
    val employmentValue = "2"
    val turnoverValue = "3"
    val prnValue = "0.2"
    val tradingStyleValue = "as"
    val legalStatusValue = "aa"

    val allVariables = Map(rurn -> rurnValue, ruref -> rurefValue, ern -> ernValue, entref -> entrefValue,
      name -> nameValue, tradingStyle -> tradingStyleValue, legalStatus -> legalStatusValue,
      address1 -> address1Value, address2 -> address2Value, address3 -> address3Value, address4 -> address4Value,
      address5 -> address5Value, postcode -> postCodeValue, sic07 -> sic07Value, employees -> employeesValue,
      employment -> employmentValue, turnover -> turnoverValue, prn -> prnValue)
    private val optionalColumns = Seq(ruref, entref, tradingStyle, legalStatus, address2, address3, address4, address5)
    val mandatoryVariables: Map[String, String] = allVariables -- optionalColumns
  }

  "A Reporting Unit row mapper" - {
    "can create a Reporting Unit when all possible variables are defined" in new Fixture {
      ReportingUnitRowMapper.fromRow(allVariables) shouldBe Some(ReportingUnit(
        rurn = Rurn(rurnValue), ruref = Some(rurefValue), ern = Ern(ernValue), entref = Some(entrefValue),
        name = nameValue, tradingStyle = Some(tradingStyleValue), legalStatus = Some(legalStatusValue),
        address1 = address1Value, address2 = Some(address2Value), address3 = Some(address3Value),
        address4 = Some(address4Value), address5 = Some(address5Value), postcode = postCodeValue,
        sic07 = sic07Value, employees = employeesValue.toInt, employment = employmentValue.toInt,
        turnover = turnoverValue.toInt, prn = prnValue
      ))
    }

    "can create a Reporting Unit when only the mandatory variables are defined" in new Fixture {
      ReportingUnitRowMapper.fromRow(mandatoryVariables) shouldBe Some(ReportingUnit(
        rurn = Rurn(rurnValue), ruref = None, ern = Ern(ernValue), entref = None,
        name = nameValue, tradingStyle = None, legalStatus = None,
        address1 = address1Value, address2 = None, address3 = None,
        address4 = None, address5 = None, postcode = postCodeValue,
        sic07 = sic07Value, employees = employeesValue.toInt, employment = employmentValue.toInt,
        turnover = turnoverValue.toInt, prn = prnValue
      ))
    }

    "cannot create a Reporting Unit when" - {
      "a mandatory variable is missing" in new Fixture {
        val mandatoryColumns = mandatoryVariables.keys
        mandatoryColumns.foreach { column =>
          withClue(s"with missing column [$column]") {
            ReportingUnitRowMapper.fromRow(mandatoryVariables - column) shouldBe None
          }
        }
      }
    }
  }
}
