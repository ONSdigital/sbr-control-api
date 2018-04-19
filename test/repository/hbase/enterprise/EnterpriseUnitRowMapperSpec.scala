package repository.hbase.enterprise

import org.scalatest.{ FreeSpec, Matchers }

import EnterpriseUnitColumns._
import support.sample.SampleEnterpriseUnit

class EnterpriseUnitRowMapperSpec extends FreeSpec with Matchers {

  private trait Fixture extends SampleEnterpriseUnit {
    val mandatoryVariables =
      Map(
        ern -> SampleEnterpriseId.value,
        entref -> SampleEnterpriseReference,
        name -> SampleEnterpriseName,
        postcode -> SamplePostcode,
        legalStatus -> SampleLegalStatus
      )

    val optionalVariables =
      Map(
        employees -> SampleNumberOfEmployees.toString,
        jobs -> SampleJobs.toString
      )

    val allVariables: Map[String, String] =
      mandatoryVariables ++ optionalVariables
  }

  "An Enterprise Unit RowMapper can make a Enterprise" - {
    "when all the fields are given" in new Fixture {
      EnterpriseUnitRowMapper.fromRow(allVariables) shouldBe Some(SampleEnterpriseWithAllFields)
    }

    "mwhen only mandatory fields are given" in new Fixture {
      println(EnterpriseUnitRowMapper.fromRow(mandatoryVariables))
      EnterpriseUnitRowMapper.fromRow(mandatoryVariables) shouldBe Some(SampleEnterpriseWithNoOptionalFields)
    }

  }

  "An Enterprise Unit RowMapper fails to create an Enterprise when" - {
    "a mandatory field is missing" in new Fixture {
      val mandatoryColumnKeys = mandatoryVariables.keys
      mandatoryColumnKeys.foreach { column =>
        withClue(s"Missing field is $column") {
          EnterpriseUnitRowMapper.fromRow(mandatoryVariables - column) shouldBe None
        }
      }
    }

    "a expected field has a mismatch with an expected type" in new Fixture {
      EnterpriseUnitRowMapper.fromRow(allVariables.updated(employees, "invalid_int")) shouldBe None
    }

    "a non-integeral value as employees" in new Fixture {
      EnterpriseUnitRowMapper.fromRow(allVariables.updated(employees, "12.90")) shouldBe None
    }

    "a non-integral value as jobs" in new Fixture {
      EnterpriseUnitRowMapper.fromRow(allVariables.updated(jobs, "456.90")) shouldBe None
    }

    "a non-integral value as both employees and jobs" in new Fixture {
      EnterpriseUnitRowMapper.fromRow(allVariables.updated(employees, "12.90").updated(jobs, "90.89")) shouldBe None
    }

  }
}