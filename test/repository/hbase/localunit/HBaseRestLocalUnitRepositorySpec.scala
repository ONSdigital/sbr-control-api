package repository.hbase.localunit

import java.time.Month.JANUARY

import scala.concurrent.Future

import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ EitherValues, FreeSpec, Matchers }

import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.localunit.{ LocalUnit, Lurn }

import repository.hbase.HBase.DefaultColumnGroup
import repository.{ RestRepository, RowMapper }
import support.sample.SampleLocalUnit

class HBaseRestLocalUnitRepositorySpec extends FreeSpec with Matchers with MockFactory with ScalaFutures with EitherValues {

  private trait Fixture extends SampleLocalUnit {
    val TargetErn = Ern("1000000013")
    val TargetPeriod: Period = Period.fromYearMonth(2018, JANUARY)
    val TargetLurn = Lurn("900000015")
    val TargetLocalUnit: LocalUnit = aLocalUnit(TargetErn, TargetLurn)
    val TargetRowKey = LocalUnitRowKey(TargetErn, TargetPeriod, TargetLurn)
    val ARow = Map("name" -> "value")
    val TargetTable = "local_unit"

    val restRepository: RestRepository = mock[RestRepository]
    val rowMapper: RowMapper[LocalUnit] = mock[RowMapper[LocalUnit]]
    val config = HBaseRestLocalUnitRepositoryConfig(TargetTable)
    val repository = new HBaseRestLocalUnitRepository(config, restRepository, rowMapper)
  }

  "A LocalUnit repository" - {
    "can retrieve a local unit by Enterprise reference (ERN), period, and Local Unit reference (LURN) when it exists" in new Fixture {
      (restRepository.findRow _).expects(TargetTable, TargetRowKey, DefaultColumnGroup).returning(Future.successful(Right(Some(ARow))))
      (rowMapper.fromRow _).expects(ARow).returning(Some(TargetLocalUnit))

      whenReady(repository.retrieveLocalUnit(TargetErn, TargetPeriod, TargetLurn)) { result =>
        result.right.value shouldBe Some(TargetLocalUnit)
      }
    }

    "returns None when a local unit with the target Enterprise reference (ERN), period, and Local Unit reference (LURN) cannot be found" in new Fixture {
      (restRepository.findRow _).expects(TargetTable, TargetRowKey, DefaultColumnGroup).returning(Future.successful(Right(None)))

      whenReady(repository.retrieveLocalUnit(TargetErn, TargetPeriod, TargetLurn)) { result =>
        result.right.value shouldBe None
      }
    }

    "signals a failure when a valid Local Unit cannot be constructed from a successful HBase REST response" in new Fixture {
      (restRepository.findRow _).expects(TargetTable, TargetRowKey, DefaultColumnGroup).returning(Future.successful(Right(Some(ARow))))
      (rowMapper.fromRow _).expects(ARow).returning(None)

      whenReady(repository.retrieveLocalUnit(TargetErn, TargetPeriod, TargetLurn)) { result =>
        result.left.value shouldBe "Unable to construct a Local Unit from Row data"
      }
    }

    "signals a failure encountered by the underlying repository" in new Fixture {
      (restRepository.findRow _).expects(TargetTable, TargetRowKey, DefaultColumnGroup).returning(Future.successful(Left("A Failure Message")))

      whenReady(repository.retrieveLocalUnit(TargetErn, TargetPeriod, TargetLurn)) { result =>
        result.left.value shouldBe "A Failure Message"
      }
    }
  }
}
