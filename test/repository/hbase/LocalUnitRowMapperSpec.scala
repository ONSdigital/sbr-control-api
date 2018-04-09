package repository.hbase

import org.scalatest.{ FreeSpec, Matchers }
import LocalUnitColumns._
import uk.gov.ons.sbr.models.enterprise.{ EnterpriseLink, Ern }
import uk.gov.ons.sbr.models.localunit.{ Address, LocalUnit, Lurn }

class LocalUnitRowMapperSpec extends FreeSpec with Matchers {

  private trait Fixture {
    val lurnValue = "ab"
    val lurefValue = "cd"
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
    val employeesValue = "34"

    val allVariables = Map(lurn -> lurnValue, luref -> lurefValue, ern -> ernValue, entref -> entrefValue,
      name -> nameValue, tradingstyle -> tradingstyleValue, address1 -> address1Value, address2 -> address2Value,
      address3 -> address3Value, address4 -> address4Value, address5 -> address5Value, postcode -> postcodeValue,
      sic07 -> sic07Value, employees -> employeesValue)
    private val optionalColumns = Seq(luref, entref, tradingstyle, address2, address3, address4, address5)
    val mandatoryVariables = allVariables -- optionalColumns
  }

  "A LocalUnit row mapper" - {
    "can create a LocalUnit when all possible variables are defined" in new Fixture {
      LocalUnitRowMapper.fromRow(allVariables) shouldBe Some(LocalUnit(Lurn(lurnValue), luref = Some(lurefValue),
        name = nameValue, tradingStyle = Some(tradingstyleValue), sic07 = sic07Value, employees = employeesValue.toInt,
        enterprise = EnterpriseLink(Ern(ernValue), entref = Some(entrefValue)),
        address = Address(line1 = address1Value, line2 = Some(address2Value), line3 = Some(address3Value),
          line4 = Some(address4Value), line5 = Some(address5Value), postcode = postcodeValue)))
    }

    "can create a LocalUnit when only the mandatory variables are defined" in new Fixture {
      LocalUnitRowMapper.fromRow(mandatoryVariables) shouldBe Some(LocalUnit(Lurn(lurnValue), luref = None,
        name = nameValue, tradingStyle = None, sic07 = sic07Value, employees = employeesValue.toInt,
        enterprise = EnterpriseLink(Ern(ernValue), entref = None),
        address = Address(line1 = address1Value, line2 = None, line3 = None, line4 = None, line5 = None, postcode = postcodeValue)))
    }

    "cannot create a LocalUnit when" - {
      "a mandatory variable is missing" in new Fixture {
        val mandatoryColumns = mandatoryVariables.keys
        mandatoryColumns.foreach { column =>
          withClue(s"with missing column [$column]") {
            LocalUnitRowMapper.fromRow(mandatoryVariables - column) shouldBe None
          }
        }
      }

      "the value of employees is non-numeric" in new Fixture {
        LocalUnitRowMapper.fromRow(allVariables.updated(employees, "non-numeric")) shouldBe None
      }

      "the value of employees is not an integral value" in new Fixture {
        LocalUnitRowMapper.fromRow(allVariables.updated(employees, "3.14159")) shouldBe None
      }
    }
  }
}
