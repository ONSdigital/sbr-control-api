package repository.hbase.localunit

import java.time.Month.JANUARY

import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ EitherValues, FreeSpec, Matchers }
import repository.hbase.HBase.DefaultColumnGroup
import repository.{ RestRepository, RowMapper }
import support.sample.SampleLocalUnit
import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.localunit.{ LocalUnit, Lurn }

import scala.concurrent.Future

class HBaseRestLocalUnitRepositorySpec extends FreeSpec with Matchers with MockFactory with ScalaFutures with EitherValues {

  private trait Fixture {
    val TargetErn = Ern("1000000013")
    val TargetPeriod = Period.fromYearMonth(2018, JANUARY)
    val TargetTable = "local_unit"

    val restRepository: RestRepository = mock[RestRepository]
    val rowMapper: RowMapper[LocalUnit] = mock[RowMapper[LocalUnit]]
    val config = HBaseRestLocalUnitRepositoryConfig(TargetTable)
    val repository = new HBaseRestLocalUnitRepository(config, restRepository, rowMapper)
  }

  private trait SingleResultFixture extends Fixture with SampleLocalUnit {
    val TargetLurn = Lurn("900000015")
    val TargetLocalUnit = aLocalUnit(TargetErn, TargetLurn)
    val TargetRowKey = LocalUnitQuery.byRowKey(TargetErn, TargetPeriod, TargetLurn)
    val ARow = Map("key" -> s"rowkey-for-${TargetLurn.value}")
  }

  private trait MultipleResultFixture extends SingleResultFixture {
    val AnotherLocalUnit = aLocalUnit(TargetErn, Lurn("900000020"))
    val AnotherRow = Map("key" -> s"rowkey-for-${AnotherLocalUnit.lurn.value}")
    val TargetQuery = LocalUnitQuery.forAllWith(TargetErn, TargetPeriod)
  }

  "A Local Unit repository" - {
    "supports retrieval of a local unit by Enterprise reference (ERN), period, and Local Unit reference (LURN)" - {
      "returning the target local unit when it exists" in new SingleResultFixture {
        (restRepository.findRow _).expects(TargetTable, TargetRowKey, DefaultColumnGroup).returning(Future.successful(Right(Some(ARow))))
        (rowMapper.fromRow _).expects(ARow).returning(Some(TargetLocalUnit))

        whenReady(repository.retrieveLocalUnit(TargetErn, TargetPeriod, TargetLurn)) { result =>
          result.right.value shouldBe Some(TargetLocalUnit)
        }
      }

      "returning nothing when the target local unit does not exist" in new SingleResultFixture {
        (restRepository.findRow _).expects(TargetTable, TargetRowKey, DefaultColumnGroup).returning(Future.successful(Right(None)))

        whenReady(repository.retrieveLocalUnit(TargetErn, TargetPeriod, TargetLurn)) { result =>
          result.right.value shouldBe None
        }
      }

      "signalling failure when a valid Local Unit cannot be constructed from a successful HBase REST response" in new SingleResultFixture {
        (restRepository.findRow _).expects(TargetTable, TargetRowKey, DefaultColumnGroup).returning(Future.successful(Right(Some(ARow))))
        (rowMapper.fromRow _).expects(ARow).returning(None)

        whenReady(repository.retrieveLocalUnit(TargetErn, TargetPeriod, TargetLurn)) { result =>
          result.left.value shouldBe "Unable to construct a Local Unit from Row data"
        }
      }

      "signalling failure when the underlying REST repository encounters a failure" in new SingleResultFixture {
        (restRepository.findRow _).expects(TargetTable, TargetRowKey, DefaultColumnGroup).returning(Future.successful(Left("A Failure Message")))

        whenReady(repository.retrieveLocalUnit(TargetErn, TargetPeriod, TargetLurn)) { result =>
          result.left.value shouldBe "A Failure Message"
        }
      }
    }

    "supports retrieval of all local units for an enterprise at a specific period in time" - {
      "returning the target local units when any exist" in new MultipleResultFixture {
        val rows = Seq(ARow, AnotherRow)
        (restRepository.findRows _).expects(TargetTable, TargetQuery, DefaultColumnGroup).returning(Future.successful(Right(rows)))
        (rowMapper.fromRow _).expects(ARow).returning(Some(TargetLocalUnit))
        (rowMapper.fromRow _).expects(AnotherRow).returning(Some(AnotherLocalUnit))

        whenReady(repository.findLocalUnitsForEnterprise(TargetErn, TargetPeriod)) { result =>
          result.right.value should contain theSameElementsAs Seq(TargetLocalUnit, AnotherLocalUnit)
        }
      }

      "returning nothing when no local units are found" in new MultipleResultFixture {
        (restRepository.findRows _).expects(TargetTable, TargetQuery, DefaultColumnGroup).returning(Future.successful(Right(Seq.empty)))

        whenReady(repository.findLocalUnitsForEnterprise(TargetErn, TargetPeriod)) { result =>
          result.right.value shouldBe empty
        }
      }

      "signalling failure when a valid Local Unit cannot be constructed from a successful HBase REST response" in new MultipleResultFixture {
        val rows = Seq(ARow, AnotherRow)
        (restRepository.findRows _).expects(TargetTable, TargetQuery, DefaultColumnGroup).returning(Future.successful(Right(rows)))
        (rowMapper.fromRow _).expects(ARow).returning(Some(TargetLocalUnit))
        (rowMapper.fromRow _).expects(AnotherRow).returning(None)

        whenReady(repository.findLocalUnitsForEnterprise(TargetErn, TargetPeriod)) { result =>
          result.left.value shouldBe "Unable to construct a Local Unit from Row data"
        }
      }

      "signalling failure when the underlying REST repository encounters a failure" in new MultipleResultFixture {
        (restRepository.findRows _).expects(TargetTable, TargetQuery, DefaultColumnGroup).returning(Future.successful(Left("A Failure Message")))

        whenReady(repository.findLocalUnitsForEnterprise(TargetErn, TargetPeriod)) { result =>
          result.left.value shouldBe "A Failure Message"
        }
      }
    }
  }
}
