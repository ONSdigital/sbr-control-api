package repository.hbase.reportingunit

import org.scalatest.{ FreeSpec, Matchers }
import repository.RestRepository.Row
import repository.hbase.reportingunit.ReportingUnitColumns._
import support.sample.SampleReportingUnit
import uk.gov.ons.sbr.models.Address
import uk.gov.ons.sbr.models.enterprise.{ EnterpriseLink, Ern }
import uk.gov.ons.sbr.models.reportingunit.{ ReportingUnit, Rurn }

class ReportingUnitRowMapperSpec extends FreeSpec with Matchers with SampleReportingUnit {

  private trait Fixture {
    val entrefValue = "9906016135"
    val rurnValue = "33000000051"
    val rurefValue = "49906016135"
    val ernValue = "1100000051"
    val nameValue = "Big Box Cereal"
    val tradingStyleValue = "Big Box Cereal Ltd"
    val address1Value = "1 Brook Court"
    val address2Value = "Bow Bridge"
    val address3Value = "Wateringbury"
    val address4Value = "Maidstone"
    val address5Value = "Kent"
    val postCodeValue = "BR3 1HG"
    val sic07Value = "96090"
    val employeesValue = "1"
    val employmentValue = "2"
    val turnoverValue = "314"
    val prnValue = "0.127698473"
    val legalStatusValue = "aa"
    val regionValue = "E12000001"

    val allVariables = Map(rurn -> rurnValue, ruref -> rurefValue, ern -> ernValue, entref -> entrefValue,
      name -> nameValue, tradingStyle -> tradingStyleValue, legalStatus -> legalStatusValue,
      address1 -> address1Value, address2 -> address2Value, address3 -> address3Value, address4 -> address4Value,
      address5 -> address5Value, postcode -> postCodeValue, sic07 -> sic07Value, employees -> employeesValue,
      employment -> employmentValue, turnover -> turnoverValue, prn -> prnValue, region -> regionValue)
    private val optionalColumns = Seq(ruref, entref, tradingStyle, address2, address3, address4, address5)
    val mandatoryVariables: Map[String, String] = allVariables -- optionalColumns
    val UnusedRowKey = ""

    def toRow(variables: Map[String, String]): Row =
      Row(rowKey = UnusedRowKey, fields = variables)
  }

  "A Reporting Unit row mapper" - {
    "can create a Reporting Unit when all possible variables are defined" in new Fixture {
      ReportingUnitRowMapper.fromRow(Row("", allVariables)) shouldBe Some(
        ReportingUnit(
          rurn = Rurn(rurnValue),
          ruref = Some(rurefValue),
          enterprise = EnterpriseLink(
            ern = Ern(ernValue),
            entref = Some(entrefValue)
          ),
          name = nameValue,
          tradingStyle = Some(tradingStyleValue),
          legalStatus = legalStatusValue,
          address = Address(
            line1 = address1Value,
            line2 = Some(address2Value),
            line3 = Some(address3Value),
            line4 = Some(address4Value),
            line5 = Some(address5Value),
            postcode = postCodeValue
          ),
          sic07 = sic07Value,
          employees = employeesValue.toInt,
          employment = employmentValue.toInt,
          turnover = turnoverValue.toInt,
          prn = BigDecimal(prnValue),
          region = regionValue
        )
      )
    }

    "can create a Reporting Unit when only the mandatory variables are defined" in new Fixture {
      ReportingUnitRowMapper.fromRow(Row("", mandatoryVariables)) shouldBe Some(
        ReportingUnit(
          rurn = Rurn(rurnValue),
          ruref = None,
          enterprise = EnterpriseLink(
            ern = Ern(ernValue),
            entref = None
          ),
          name = nameValue,
          tradingStyle = None,
          legalStatus = legalStatusValue,
          address = Address(
            line1 = address1Value,
            line2 = None,
            line3 = None,
            line4 = None,
            line5 = None,
            postcode = postCodeValue
          ),
          sic07 = sic07Value,
          employees = employeesValue.toInt,
          employment = employmentValue.toInt,
          turnover = turnoverValue.toInt,
          prn = BigDecimal(prnValue),
          region = regionValue
        )
      )
    }

    "cannot create a Reporting Unit when" - {
      "a mandatory variable is missing" in new Fixture {
        val mandatoryColumns = mandatoryVariables.keys
        mandatoryColumns.foreach { column =>
          withClue(s"with missing column [$column]") {
            ReportingUnitRowMapper.fromRow(toRow(mandatoryVariables - column)) shouldBe None
          }
        }
      }

      "a non-numeric value" - {
        "is found for employees" in new Fixture {
          ReportingUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = allVariables.updated(employees, "invalid_int"))) shouldBe None
        }

        "is found for employment" in new Fixture {
          ReportingUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = allVariables.updated(employment, "invalid_int"))) shouldBe None
        }

        "is found for turnover" in new Fixture {
          ReportingUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = allVariables.updated(turnover, "invalid_int"))) shouldBe None
        }

        "is found for prn" in new Fixture {
          ReportingUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = allVariables.updated(prn, "invalid_bigDecimal"))) shouldBe None
        }
      }

      "a non-integral value" - {
        "is found for employees" in new Fixture {
          ReportingUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = allVariables.updated(employees, "12.90"))) shouldBe None
        }

        "is found for employment" in new Fixture {
          ReportingUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = allVariables.updated(employment, "11.90"))) shouldBe None
        }

        "is found for turnover" in new Fixture {
          ReportingUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = allVariables.updated(turnover, "100.99"))) shouldBe None
        }
      }
    }
  }
}
