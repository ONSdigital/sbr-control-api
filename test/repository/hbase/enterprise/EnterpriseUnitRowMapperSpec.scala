package repository.hbase.enterprise

import org.scalatest.{ FreeSpec, Matchers }

import repository.hbase.unit.enterprise.EnterpriseUnitColumns._
import repository.hbase.unit.enterprise.EnterpriseUnitRowMapper
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

  "An Enterprise Unit RowMapper can" - {
    "make a Enterprise when all the fields are given" in new Fixture {
      EnterpriseUnitRowMapper.fromRow(allVariables) shouldBe Some(SampleEnterpriseWithAllFields)
    }

    "make a Enterprise with only mandatory fields" in new Fixture {
      println(EnterpriseUnitRowMapper.fromRow(mandatoryVariables))
      EnterpriseUnitRowMapper.fromRow(mandatoryVariables) shouldBe Some(SampleEnterpriseWithNoOptionalFields)
    }

  }

  "Fails to create a Enterprise Unit RowMapper when" - {
    "a mandatory field is missing" in new Fixture {
      val mandatoryColumnKeys: Iterable[String] = mandatoryVariables.keys
      mandatoryColumnKeys.foreach { column =>
        withClue(s"Missing field is $column") {
          EnterpriseUnitRowMapper.fromRow(mandatoryVariables - column) shouldBe None
        }
      }
    }

    "a expected field has a mismatch with an expected type" in new Fixture {
      EnterpriseUnitRowMapper.fromRow(allVariables.updated(employees, "invalid_int")) shouldBe None
    }
  }
}