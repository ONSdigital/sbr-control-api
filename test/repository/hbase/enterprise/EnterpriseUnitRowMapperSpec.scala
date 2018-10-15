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
        legalStatus -> SampleLegalStatus,
        prn -> SamplePrn.toString(),
        workingProprietors -> SampleWorkingProprietors.toString,
        employment -> SampleEmployment.toString,
        region -> SampleRegion
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
        enterpriseTurnover -> SampleEnterpriseTurnover.toString,
        imputedEmployees -> SampleImputedEmployees.toString,
        imputedTurnover -> SampleImputedTurnover.toString
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

      /*
       * In this scenario - the optional sub-objects for turnover & imputed will be omitted completely
       */
      "when only mandatory fields are given" in new Fixture {
        EnterpriseUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = mandatoryVariables)) shouldBe Some(SampleEnterpriseWithNoOptionalFields)
      }

      "with partially populated turnover values" in new Fixture {
        val variablesWithPartialTurnover = allVariables -- Seq(standardTurnover, containedTurnover)
        val expectedEnterprise = SampleEnterpriseWithAllFields.copy(turnover = Some(
          SampleTurnoverWithAllFields.copy(standardTurnover = None, containedTurnover = None)
        ))

        EnterpriseUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = variablesWithPartialTurnover)) shouldBe Some(expectedEnterprise)
      }

      "with partially populated imputed values" in new Fixture {
        val variablesWithPartialImputation = allVariables - imputedTurnover
        val expectedEnterprise = SampleEnterpriseWithAllFields.copy(imputed = Some(
          SampleImputedWithAllFields.copy(turnover = None)
        ))

        EnterpriseUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = variablesWithPartialImputation)) shouldBe Some(expectedEnterprise)
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

      "a non-numeric value is found for" - {
        "employees" in new Fixture {
          EnterpriseUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = allVariables.updated(employees, "invalid_int"))) shouldBe None
        }

        "jobs" in new Fixture {
          EnterpriseUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = allVariables.updated(jobs, "invalid_int"))) shouldBe None
        }

        "contained turnover" in new Fixture {
          EnterpriseUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = allVariables.updated(containedTurnover, "invalid_int"))) shouldBe None
        }

        "standard turnover" in new Fixture {
          EnterpriseUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = allVariables.updated(standardTurnover, "invalid_int"))) shouldBe None
        }

        "group turnover" in new Fixture {
          EnterpriseUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = allVariables.updated(groupTurnover, "invalid_int"))) shouldBe None
        }

        "apportioned turnover" in new Fixture {
          EnterpriseUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = allVariables.updated(apportionedTurnover, "invalid_int"))) shouldBe None
        }

        "enterprise turnover" in new Fixture {
          EnterpriseUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = allVariables.updated(enterpriseTurnover, "invalid_int"))) shouldBe None
        }

        "prn" in new Fixture {
          EnterpriseUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = allVariables.updated(prn, "invalid_bigDecimal"))) shouldBe None
        }

        "workingProprietors" in new Fixture {
          EnterpriseUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = allVariables.updated(workingProprietors, "invalid_int"))) shouldBe None
        }

        "employment" in new Fixture {
          EnterpriseUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = allVariables.updated(employment, "invalid_int"))) shouldBe None
        }

        "imputed employees" in new Fixture {
          EnterpriseUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = allVariables.updated(imputedEmployees, "invalid_int"))) shouldBe None
        }

        "imputed turnover" in new Fixture {
          EnterpriseUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = allVariables.updated(imputedTurnover, "invalid_int"))) shouldBe None
        }
      }

      "a non-integral value is found for" - {
        "employees" in new Fixture {
          EnterpriseUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = allVariables.updated(employees, "12.90"))) shouldBe None
        }

        "jobs" in new Fixture {
          EnterpriseUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = allVariables.updated(jobs, "456.90"))) shouldBe None
        }

        "contained turnover" in new Fixture {
          EnterpriseUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = allVariables.updated(containedTurnover, "12.90"))) shouldBe None
        }

        "standard turnover" in new Fixture {
          EnterpriseUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = allVariables.updated(standardTurnover, "456.90"))) shouldBe None
        }

        "group turnover" in new Fixture {
          EnterpriseUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = allVariables.updated(groupTurnover, "12.90"))) shouldBe None
        }

        "apportioned turnover" in new Fixture {
          EnterpriseUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = allVariables.updated(apportionedTurnover, "456.90"))) shouldBe None
        }

        "enterprise turnover" in new Fixture {
          EnterpriseUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = allVariables.updated(enterpriseTurnover, "456.90"))) shouldBe None
        }

        "workingProprietors" in new Fixture {
          EnterpriseUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = allVariables.updated(workingProprietors, "2.5"))) shouldBe None
        }

        "employment" in new Fixture {
          EnterpriseUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = allVariables.updated(employment, "100.5"))) shouldBe None
        }

        "imputed employees" in new Fixture {
          EnterpriseUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = allVariables.updated(imputedEmployees, "5.5"))) shouldBe None
        }

        "imputed turnover" in new Fixture {
          EnterpriseUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = allVariables.updated(imputedTurnover, "4999.99"))) shouldBe None
        }
      }
    }
  }
}