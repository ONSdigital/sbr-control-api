package repository.hbase.enterprise

import java.time.Month.SEPTEMBER

import scala.concurrent.Future

import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ EitherValues, FreeSpec, Matchers }

import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.Enterprise

import repository.RestRepository.Row
import repository.hbase.HBase.DefaultColumnFamily
import repository.{ RestRepository, RowMapper }
import support.sample.SampleEnterpriseUnit

class HBaseRestEnterpriseUnitRepositorySpec extends FreeSpec with Matchers with MockFactory with ScalaFutures with EitherValues {

  private trait Fixture extends SampleEnterpriseUnit {
    val TargetPeriod: Period = Period.fromYearMonth(2018, SEPTEMBER)
    val TargetBaseTable = "enterprise"
    val TargetPeriodTable = s"${TargetBaseTable}_${Period.asString(TargetPeriod)}"
    val TargetErn = SampleEnterpriseId
    val TargetRowKey = EnterpriseUnitRowKey(TargetErn)

    private val UnusedRowKey = ""
    val ARow: Row = Row(rowKey = UnusedRowKey, fields = Map("name" -> "value"))
    val TargetEnterpriseUnit: Enterprise = aEnterpriseSample(TargetErn)
    val TargetExpectedFailureMessage = "A Failure Message"

    val restRepository: RestRepository = mock[RestRepository]
    val rowMapper: RowMapper[Enterprise] = mock[RowMapper[Enterprise]]
    val config = HBaseRestEnterpriseUnitRepositoryConfig(TargetBaseTable)
    val repository = new HBaseRestEnterpriseUnitRepository(restRepository, config, rowMapper)
  }

  "A Enterprise Unit Repository" - {
    "can retrieve an enterprise unit by Enterprise Reference Number (ERN) and a period (yyyyMM) when a Enterprise exists" in new Fixture {
      (restRepository.findRow _).expects(TargetPeriodTable, TargetRowKey, DefaultColumnFamily).returning(Future.successful(Right(Some(ARow))))
      (rowMapper.fromRow _).expects(ARow).returning(Some(TargetEnterpriseUnit))

      whenReady(repository.retrieveEnterpriseUnit(TargetErn, TargetPeriod)) { result =>
        result.right.value shouldBe Some(TargetEnterpriseUnit)
      }
    }

    "returns none when an enterprise unit by Enterprise Reference Number (ERN) and a period (yyyyMM) cannot be found" in new Fixture {
      (restRepository.findRow _).expects(TargetPeriodTable, TargetRowKey, DefaultColumnFamily).returning(Future.successful(Right(None)))

      whenReady(repository.retrieveEnterpriseUnit(TargetErn, TargetPeriod)) { result =>
        result.right.value shouldBe None
      }
    }

    "prompts a failure when a valid Enterprise Unit cannot be constructed from a successful HBase response" in new Fixture {
      (restRepository.findRow _).expects(TargetPeriodTable, TargetRowKey, DefaultColumnFamily).returning(Future.successful(Right(Some(ARow))))
      (rowMapper.fromRow _).expects(ARow).returning(None)

      whenReady(repository.retrieveEnterpriseUnit(TargetErn, TargetPeriod)) { result =>
        result.left.value shouldBe "Unable to construct a Enterprise Unit from Row data"
      }
    }

    "prompts an encountered failure in the underlying repository layer" in new Fixture {
      (restRepository.findRow _).expects(TargetPeriodTable, TargetRowKey, DefaultColumnFamily).returning(Future.successful(Left(TargetExpectedFailureMessage)))

      whenReady(repository.retrieveEnterpriseUnit(TargetErn, TargetPeriod)) { result =>
        result.left.value shouldBe TargetExpectedFailureMessage
      }
    }

  }
}
