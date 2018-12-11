package repository.hbase.reportingunit

import java.time.Month._

import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{EitherValues, FreeSpec, Matchers}
import repository.RestRepository.Row
import repository.hbase.HBase._
import repository.{RestRepository, RowMapper}
import support.sample.SampleReportingUnit
import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.reportingunit.{ReportingUnit, Rurn}

import scala.concurrent.{ExecutionContext, Future}

class HBaseRestReportingUnitRepositorySpec extends FreeSpec with Matchers with MockFactory with ScalaFutures with EitherValues {
  private trait Fixture {
    val TargetErn = Ern("1000000013")
    val TargetPeriod = Period.fromYearMonth(2018, JANUARY)
    val TargetBaseTable = "reporting_unit"
    val TargetPeriodTable = s"${TargetBaseTable}_${Period.asString(TargetPeriod)}"

    val restRepository: RestRepository = mock[RestRepository]
    val rowMapper: RowMapper[ReportingUnit] = mock[RowMapper[ReportingUnit]]
    val config = HBaseRestReportingUnitRepositoryConfig(TargetBaseTable)
    val repository = new HBaseRestReportingUnitRepository(config, restRepository, rowMapper)(ExecutionContext.global)
  }

  private trait SingleResultFixture extends Fixture with SampleReportingUnit {
    val TargetRurn = Rurn("33000000000")
    val TargetReportingUnit = aReportingUnit(TargetErn, TargetRurn)
    val TargetRowKey = ReportingUnitQuery.byRowKey(TargetErn, TargetRurn)

    private val UnusedRowKey = ""
    val ARow = Map("key" -> s"rowkey-for-${TargetRurn.value}")
    def toRow(variables: Map[String, String]) = Row(rowKey = UnusedRowKey, fields = variables)
  }

  private trait MultipleResultFixture extends SingleResultFixture {
    val AnotherReportingUnit = aReportingUnit(TargetErn, Rurn("33000000000"))
    val AnotherRow = Map("key" -> s"rowkey-for-${AnotherReportingUnit.rurn.value}")
    val TargetQuery = ReportingUnitQuery.forAllWith(TargetErn)
  }

  "A Reporting Unit repository" - {
    "supports retrieval of a reporting unit by Enterprise reference (ERN), period, and Reporting Unit reference (RURN)" - {
      "returning the target reporting unit when it exists" in new SingleResultFixture {
        (restRepository.findRow _).expects(TargetPeriodTable, TargetRowKey, DefaultColumnFamily).returning(Future.successful(Right(Some(toRow(ARow)))))
        (rowMapper.fromRow _).expects(toRow(ARow)).returning(Some(TargetReportingUnit))

        whenReady(repository.retrieveReportingUnit(TargetErn, TargetPeriod, TargetRurn)) { result =>
          result.right.value shouldBe Some(TargetReportingUnit)
        }
      }

      "returning nothing when the target reporting unit does not exist" in new SingleResultFixture {
        (restRepository.findRow _).expects(TargetPeriodTable, TargetRowKey, DefaultColumnFamily).returning(Future.successful(Right(None)))

        whenReady(repository.retrieveReportingUnit(TargetErn, TargetPeriod, TargetRurn)) { result =>
          result.right.value shouldBe None
        }
      }

      "signalling failure when a valid reporting unit cannot be constructed from a successful HBase REST response" in new SingleResultFixture {
        (restRepository.findRow _).expects(TargetPeriodTable, TargetRowKey, DefaultColumnFamily).returning(Future.successful(Right(Some(toRow(ARow)))))
        (rowMapper.fromRow _).expects(toRow(ARow)).returning(None)

        whenReady(repository.retrieveReportingUnit(TargetErn, TargetPeriod, TargetRurn)) { result =>
          result.left.value shouldBe "Unable to construct a Reporting Unit from Row data"
        }
      }

      "signalling failure when the underlying REST repository encounters a failure" in new SingleResultFixture {
        (restRepository.findRow _).expects(TargetPeriodTable, TargetRowKey, DefaultColumnFamily).returning(Future.successful(Left("A Failure Message")))

        whenReady(repository.retrieveReportingUnit(TargetErn, TargetPeriod, TargetRurn)) { result =>
          result.left.value shouldBe "A Failure Message"
        }
      }
    }

    "supports retrieval of all reporting units for an enterprise at a specific period in time" - {
      "returning the target reporting units when any exist" in new MultipleResultFixture {
        val rows = Seq(ARow, AnotherRow)
        (restRepository.findRows _).expects(TargetPeriodTable, TargetQuery, DefaultColumnFamily).returning(Future.successful(Right(rows.map(toRow))))
        (rowMapper.fromRow _).expects(toRow(ARow)).returning(Some(TargetReportingUnit))
        (rowMapper.fromRow _).expects(toRow(AnotherRow)).returning(Some(AnotherReportingUnit))

        whenReady(repository.findReportingUnitsForEnterprise(TargetErn, TargetPeriod)) { result =>
          result.right.value should contain theSameElementsAs Seq(TargetReportingUnit, AnotherReportingUnit)
        }
      }

      "returning nothing when no reporting units are found" in new MultipleResultFixture {
        (restRepository.findRows _).expects(TargetPeriodTable, TargetQuery, DefaultColumnFamily).returning(Future.successful(Right(Seq.empty)))

        whenReady(repository.findReportingUnitsForEnterprise(TargetErn, TargetPeriod)) { result =>
          result.right.value shouldBe empty
        }
      }

      "signalling failure when a valid reporting unit cannot be constructed from a successful HBase REST response" in new MultipleResultFixture {
        val rows = Seq(ARow, AnotherRow)
        (restRepository.findRows _).expects(TargetPeriodTable, TargetQuery, DefaultColumnFamily).returning(Future.successful(Right(rows.map(toRow))))
        (rowMapper.fromRow _).expects(toRow(ARow)).returning(Some(TargetReportingUnit))
        (rowMapper.fromRow _).expects(toRow(AnotherRow)).returning(None)

        whenReady(repository.findReportingUnitsForEnterprise(TargetErn, TargetPeriod)) { result =>
          result.left.value shouldBe "Unable to construct a Reporting Unit from Row data"
        }
      }

      "signalling failure when the underlying REST repository encounters a failure" in new MultipleResultFixture {
        (restRepository.findRows _).expects(TargetPeriodTable, TargetQuery, DefaultColumnFamily).returning(Future.successful(Left("A Failure Message")))

        whenReady(repository.findReportingUnitsForEnterprise(TargetErn, TargetPeriod)) { result =>
          result.left.value shouldBe "A Failure Message"
        }
      }
    }
  }
}
