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

    val variables = Map(lurn -> lurnValue, luref -> lurefValue, ern -> ernValue, entref -> entrefValue,
      name -> nameValue, tradingstyle -> tradingstyleValue, address1 -> address1Value, address2 -> address2Value,
      address3 -> address3Value, address4 -> address4Value, address5 -> address5Value, postcode -> postcodeValue,
      sic07 -> sic07Value, employees -> employeesValue)
  }

  "A LocalUnit row mapper" - {
    "can create a LocalUnit from a set of variables" in new Fixture {
      LocalUnitRowMapper.fromRow(variables) shouldBe Some(LocalUnit(Lurn(lurnValue), luref = lurefValue, name = nameValue,
        tradingStyle = tradingstyleValue, sic07 = sic07Value, employees = employeesValue.toInt,
        enterprise = EnterpriseLink(Ern(ernValue), entref = entrefValue),
        address = Address(line1 = address1Value, line2 = address2Value, line3 = address3Value, line4 = address4Value,
          line5 = address5Value, postcode = postcodeValue)))
    }

    "cannot create a LocalUnit when" - {
      "any variable is missing" in new Fixture {
        val keys = Seq(lurn, luref, ern, entref, name, tradingstyle, address1, address2, address3, address4, address5,
          postcode, sic07, employees)

        keys.foreach { key =>
          withClue(s"with missing key [$key]") {
            LocalUnitRowMapper.fromRow(variables - key) shouldBe None
          }
        }
      }

      "the value of employees is non-numeric" in new Fixture {
        LocalUnitRowMapper.fromRow(variables.updated(employees, "non-numeric")) shouldBe None
      }

      "the value of employees is not an integral value" in new Fixture {
        LocalUnitRowMapper.fromRow(variables.updated(employees, "3.14159")) shouldBe None
      }
    }
  }
}
