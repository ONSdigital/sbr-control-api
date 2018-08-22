package repository.hbase.legalunit

import java.time.Month.JANUARY

import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ EitherValues, FreeSpec, Matchers }
import repository.RestRepository.Row
import repository.hbase.HBase.DefaultColumnFamily
import repository.{ RestRepository, RowMapper }
import support.sample.SampleLegalUnit
import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.legalunit.{ LegalUnit, Ubrn }

import scala.concurrent.Future

class HBaseRestLegalUnitRepositorySpec extends FreeSpec with Matchers with MockFactory with ScalaFutures with EitherValues {

  private trait Fixture {
    val TargetErn = Ern("1000000013")
    val TargetPeriod = Period.fromYearMonth(2018, JANUARY)
    val TargetBaseTable = "legal_unit"
    val TargetPeriodTable = s"${TargetBaseTable}_${Period.asString(TargetPeriod)}"

    val restRepository: RestRepository = mock[RestRepository]
    val rowMapper: RowMapper[LegalUnit] = mock[RowMapper[LegalUnit]]
    val config = HBaseRestLegalUnitRepositoryConfig(TargetBaseTable)
    val repository = new HBaseRestLegalUnitRepository(config, restRepository, rowMapper)

    private val UnusedRowKey = ""
    def toRow(variables: Map[String, String]) = Row(rowKey = UnusedRowKey, fields = variables)
  }

  private trait SingleResultFixture extends Fixture with SampleLegalUnit {
    val TargetUbrn = Ubrn("1010101900000015")
    val TargetLegalUnit = aLegalUnit(TargetUbrn)
    val TargetRowKey = LegalUnitQuery.byRowKey(TargetErn, TargetUbrn)
    val ARow = Map("key" -> s"rowkey-for-${TargetUbrn.value}")
  }

  private trait MultipleResultFixture extends SingleResultFixture {
    val AnotherLegalUnit = aLegalUnit(Ubrn("9000000201234567"))
    val AnotherRow = Map("key" -> s"rowkey-for-${AnotherLegalUnit.ubrn.value}")
    val TargetQuery = LegalUnitQuery.forAllWith(TargetErn)
  }

  "A Legal Unit repository" - {
    "supports retrieval of a legal unit by Enterprise reference (ERN), period, and Legal Unit reference (UBRN)" - {
      "returning the target legal unit when it exists" in new SingleResultFixture {
        (restRepository.findRow _).expects(TargetPeriodTable, TargetRowKey, DefaultColumnFamily).returning(
          Future.successful(Right(Some(toRow(ARow))))
        )
        (rowMapper.fromRow _).expects(toRow(ARow)).returning(Some(TargetLegalUnit))

        whenReady(repository.retrieveLegalUnit(TargetErn, TargetPeriod, TargetUbrn)) { result =>
          result.right.value shouldBe Some(TargetLegalUnit)
        }
      }

      "returning nothing when the target legal unit does not exist" in new SingleResultFixture {
        (restRepository.findRow _).expects(TargetPeriodTable, TargetRowKey, DefaultColumnFamily).returning(
          Future.successful(Right(None))
        )

        whenReady(repository.retrieveLegalUnit(TargetErn, TargetPeriod, TargetUbrn)) { result =>
          result.right.value shouldBe None
        }
      }

      "signalling failure when a valid Legal Unit cannot be constructed from a successful HBase REST response" in new SingleResultFixture {
        (restRepository.findRow _).expects(TargetPeriodTable, TargetRowKey, DefaultColumnFamily).returning(
          Future.successful(Right(Some(toRow(ARow))))
        )
        (rowMapper.fromRow _).expects(toRow(ARow)).returning(None)

        whenReady(repository.retrieveLegalUnit(TargetErn, TargetPeriod, TargetUbrn)) { result =>
          result.left.value shouldBe "Unable to construct a Legal Unit from Row data"
        }
      }

      "signalling failure when the underlying REST repository encounters a failure" in new SingleResultFixture {
        (restRepository.findRow _).expects(TargetPeriodTable, TargetRowKey, DefaultColumnFamily).returning(
          Future.successful(Left("A Failure Message"))
        )

        whenReady(repository.retrieveLegalUnit(TargetErn, TargetPeriod, TargetUbrn)) { result =>
          result.left.value shouldBe "A Failure Message"
        }
      }
    }

    "supports retrieval of all legal units for an enterprise at a specific period in time" - {
      "returning the target legal units when any exist" in new MultipleResultFixture {
        val rows = Seq(ARow, AnotherRow)
        (restRepository.findRows _).expects(TargetPeriodTable, TargetQuery, DefaultColumnFamily).returning(
          Future.successful(Right(rows.map(toRow)))
        )
        (rowMapper.fromRow _).expects(toRow(ARow)).returning(Some(TargetLegalUnit))
        (rowMapper.fromRow _).expects(toRow(AnotherRow)).returning(Some(AnotherLegalUnit))

        whenReady(repository.findLegalUnitsForEnterprise(TargetErn, TargetPeriod)) { result =>
          result.right.value should contain theSameElementsAs Seq(TargetLegalUnit, AnotherLegalUnit)
        }
      }

      "returning nothing when no legal units are found" in new MultipleResultFixture {
        (restRepository.findRows _).expects(TargetPeriodTable, TargetQuery, DefaultColumnFamily).returning(
          Future.successful(Right(Seq.empty))
        )

        whenReady(repository.findLegalUnitsForEnterprise(TargetErn, TargetPeriod)) { result =>
          result.right.value shouldBe empty
        }
      }

      "signalling failure when a valid Legal Unit cannot be constructed from a successful HBase REST response" in new MultipleResultFixture {
        val rows = Seq(ARow, AnotherRow)
        (restRepository.findRows _).expects(TargetPeriodTable, TargetQuery, DefaultColumnFamily).returning(
          Future.successful(Right(rows.map(toRow)))
        )
        (rowMapper.fromRow _).expects(toRow(ARow)).returning(Some(TargetLegalUnit))
        (rowMapper.fromRow _).expects(toRow(AnotherRow)).returning(None)

        whenReady(repository.findLegalUnitsForEnterprise(TargetErn, TargetPeriod)) { result =>
          result.left.value shouldBe "Unable to construct a Legal Unit from Row data"
        }
      }

      "signalling failure when the underlying REST repository encounters a failure" in new MultipleResultFixture {
        (restRepository.findRows _).expects(TargetPeriodTable, TargetQuery, DefaultColumnFamily).returning(
          Future.successful(Left("A Failure Message"))
        )

        whenReady(repository.findLegalUnitsForEnterprise(TargetErn, TargetPeriod)) { result =>
          result.left.value shouldBe "A Failure Message"
        }
      }
    }
  }
}
