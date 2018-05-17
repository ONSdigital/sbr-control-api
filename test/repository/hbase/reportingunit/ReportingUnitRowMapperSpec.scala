package repository.hbase.reportingunit

import org.scalatest.{ FreeSpec, Matchers }

import repository.hbase.reportingunit.ReportingUnitColumns._
import uk.gov.ons.sbr.models.reportingunit.{ ReportingUnit, Rurn }

import repository.RestRepository.Row

class ReportingUnitRowMapperSpec extends FreeSpec with Matchers {

  private trait Fixture {
    val rurnValue = "ab"
    val rurefValue = "cd"

    val allVariables = Map(rurn -> rurnValue, ruref -> rurefValue)
    private val optionalColumns = Seq(ruref)
    val mandatoryVariables: Map[String, String] = allVariables -- optionalColumns
    private val UnusedRowKey = ""

    def toRow(variables: Map[String, String]) = Row(rowKey = UnusedRowKey, fields = variables)
  }

  "A Reporting Unit row mapper" - {
    "can create a Reporting Unit when all possible variables are defined" in new Fixture {
      ReportingUnitRowMapper.fromRow(toRow(allVariables)) shouldBe Some(ReportingUnit(Rurn(rurnValue), ruref = Some(rurefValue)))
    }

    "can create a Reporting Unit when only the mandatory variables are defined" in new Fixture {
      ReportingUnitRowMapper.fromRow(toRow(mandatoryVariables)) shouldBe Some(ReportingUnit(Rurn(rurnValue), ruref = None))
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
    }
  }
}
