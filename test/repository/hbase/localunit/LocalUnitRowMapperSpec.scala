package repository.hbase.localunit

import org.scalatest.{ FreeSpec, Matchers }

import uk.gov.ons.sbr.models.Address
import uk.gov.ons.sbr.models.enterprise.{ EnterpriseLink, Ern }
import uk.gov.ons.sbr.models.localunit.{ LocalUnit, Lurn }
import uk.gov.ons.sbr.models.reportingunit.{ ReportingUnitLink, Rurn }

import repository.RestRepository.Row
import repository.hbase.localunit.LocalUnitColumns._

class LocalUnitRowMapperSpec extends FreeSpec with Matchers {

  private trait Fixture {
    val lurnValue = "222226789"
    val lurefValue = "888886789"
    val ernValue = "1100000001"
    val entrefValue = "9990009991"
    val rurnValue = "33000000051"
    val rurefValue = "49906016135"
    val nameValue = "Big Box Cereal"
    val tradingStyleValue = "Big Box Cereal Ltd"
    val address1Value = "1 Black Barn Cottages"
    val address2Value = "Saxtead Road"
    val address3Value = "Framlingham"
    val address4Value = "Greatstone"
    val address5Value = "Kent"
    val postcodeValue = "TN28 8NX"
    val sic07Value = "10612"
    val employeesValue = "34"
    val employmentValue = "35"
    val prnValue = "0.016587362"
    val regionValue = "E12000001"

    val allVariables = Map(lurn -> lurnValue, luref -> lurefValue, ern -> ernValue, entref -> entrefValue,
      name -> nameValue, rurn -> rurnValue, ruref -> rurefValue, tradingStyle -> tradingStyleValue, address1 -> address1Value,
      address2 -> address2Value, address3 -> address3Value, address4 -> address4Value, address5 -> address5Value,
      postcode -> postcodeValue, sic07 -> sic07Value, employees -> employeesValue, employment -> employmentValue,
      prn -> prnValue, region -> regionValue)
    private val optionalColumns = Seq(luref, entref, ruref, tradingStyle, address2, address3, address4, address5)
    val mandatoryVariables: Map[String, String] = allVariables -- optionalColumns
    val UnusedRowKey = ""
  }

  "A LocalUnit row mapper" - {
    "can create a LocalUnit when all possible variables are defined" in new Fixture {
      LocalUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = allVariables)) shouldBe Some(LocalUnit(
        Lurn(lurnValue),
        luref = Some(lurefValue),
        name = nameValue,
        tradingStyle = Some(tradingStyleValue),
        sic07 = sic07Value,
        employees = employeesValue.toInt,
        employment = employmentValue.toInt,
        enterprise = EnterpriseLink(Ern(ernValue), entref = Some(entrefValue)),
        reportingUnit = ReportingUnitLink(Rurn(rurnValue), ruref = Some(rurefValue)),
        address = Address(line1 = address1Value, line2 = Some(address2Value), line3 = Some(address3Value),
          line4 = Some(address4Value), line5 = Some(address5Value), postcode = postcodeValue),
        region = regionValue,
        prn = BigDecimal(prnValue)
      ))
    }

    "can create a LocalUnit when only the mandatory variables are defined" in new Fixture {
      LocalUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = mandatoryVariables)) shouldBe Some(LocalUnit(
        Lurn(lurnValue),
        luref = None,
        name = nameValue,
        tradingStyle = None,
        sic07 = sic07Value,
        employees = employeesValue.toInt,
        employment = employmentValue.toInt,
        enterprise = EnterpriseLink(Ern(ernValue), entref = None),
        reportingUnit = ReportingUnitLink(Rurn(rurnValue), ruref = None),
        address = Address(line1 = address1Value, line2 = None, line3 = None, line4 = None, line5 = None, postcode = postcodeValue),
        region = regionValue,
        prn = BigDecimal(prnValue)
      ))
    }

    "cannot create a LocalUnit when" - {
      "a mandatory variable is missing" in new Fixture {
        val mandatoryColumns = mandatoryVariables.keys
        mandatoryColumns.foreach { column =>
          withClue(s"with missing column [$column]") {
            LocalUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = mandatoryVariables - column)) shouldBe None
          }
        }
      }

      "the value of employees is" - {
        "non-numeric" in new Fixture {
          LocalUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = allVariables.updated(employees, "non-numeric"))) shouldBe None
        }

        "not an integral value" in new Fixture {
          LocalUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = allVariables.updated(employees, "3.14159"))) shouldBe None
        }
      }

      "the value of employment is" - {
        "non-numeric" in new Fixture {
          LocalUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = allVariables.updated(employment, "non-numeric"))) shouldBe None
        }

        "not an integral value" in new Fixture {
          LocalUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = allVariables.updated(employment, "1.5"))) shouldBe None
        }
      }

      "the value of prn is" - {
        "non-numeric" in new Fixture {
          LocalUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = allVariables.updated(prn, "non-numeric"))) shouldBe None
        }
      }
    }
  }
}
