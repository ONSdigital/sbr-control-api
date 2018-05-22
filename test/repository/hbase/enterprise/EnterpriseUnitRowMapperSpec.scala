package repository.hbase.enterprise

import org.scalatest.{ FreeSpec, Matchers }

import EnterpriseUnitColumns._
import repository.RestRepository.Row
import support.sample.SampleEnterpriseUnit

class EnterpriseUnitRowMapperSpec extends FreeSpec with Matchers {

  private trait Fixture extends SampleEnterpriseUnit {
    val mandatoryVariables =
      Map(
        ern -> SampleEnterpriseId.value,
        name -> SampleEnterpriseName,
        address1 -> SampleAddressLine1,
        postcode -> SamplePostcode,
        sic07 -> SampleSIC07,
        legalStatus -> SampleLegalStatus
      )

    val optionalVariables =
      Map(
        entref -> SampleEnterpriseReference,
        tradingStyle -> SampleTradingStyle,
        address2 -> SampleAddressLine2,
        address3 -> SampleAddressLine3,
        address4 -> SampleAddressLine4,
        address5 -> SampleAddressLine5,
        employees -> SampleNumberOfEmployees.toString,
        jobs -> SampleJobs.toString,
        containedTurnover -> SampleContainedTurnover.toString,
        standardTurnover -> SampleStandardTurnover.toString,
        groupTurnover -> SampleGroupTurnover.toString,
        apportionedTurnover -> SampleApportionedTurnover.toString,
        enterpriseTurnover -> SampleEnterpriseTurnover.toString
      )

    val allVariables: Map[String, String] =
      mandatoryVariables ++ optionalVariables

    val UnusedRowKey = ""
  }

  "An Enterprise Unit RowMapper" - {
    "can make an Enterprise" - {
      "when all the fields are given" in new Fixture {
        EnterpriseUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = allVariables)) shouldBe Some(SampleEnterpriseWithAllFields)
      }

      "when only mandatory fields are given" in new Fixture {
        EnterpriseUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = mandatoryVariables)) shouldBe Some(SampleEnterpriseWithNoOptionalFields)
      }

    }

    "fails to create an Enterprise when" - {
      "a mandatory field is missing" in new Fixture {
        val mandatoryColumnKeys = mandatoryVariables.keys
        mandatoryColumnKeys.foreach { column =>
          withClue(s"Missing field is $column") {
            EnterpriseUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = mandatoryVariables - column)) shouldBe None
          }
        }
      }

      "a non-numeric value is found for employees" in new Fixture {
        EnterpriseUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = allVariables.updated(employees, "invalid_int"))) shouldBe None
      }

      "a non-numeric value is found for jobs" in new Fixture {
        EnterpriseUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = allVariables.updated(jobs, "invalid_int"))) shouldBe None
      }

      "a non-integeral value is found for employees" in new Fixture {
        EnterpriseUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = allVariables.updated(employees, "12.90"))) shouldBe None
      }

      "a non-integral value is found for jobs" in new Fixture {
        EnterpriseUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = allVariables.updated(jobs, "456.90"))) shouldBe None
      }

      "a non-integral value is found for employees and jobs" in new Fixture {
        EnterpriseUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = allVariables.updated(employees, "12.90").updated(jobs, "90.89"))) shouldBe None
      }
    }
  }

}