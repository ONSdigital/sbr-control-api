package services

import java.time.Month.AUGUST

import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FreeSpec, Matchers}
import repository.RestRepository
import repository.RestRepository.Row
import repository.hbase.unitlinks.HBaseRestUnitLinksRepository.ColumnFamily
import repository.hbase.unitlinks.{HBaseRestUnitLinksRepositoryConfig, UnitLinksRowKey}
import uk.gov.ons.sbr.models.unitlinks.UnitId
import uk.gov.ons.sbr.models.unitlinks.UnitType.LegalUnit
import uk.gov.ons.sbr.models.{Period, UnitKey}

import scala.concurrent.{ExecutionContext, Future}

class UnitLinkUnitRegisterServiceSpec extends FreeSpec with Matchers with MockFactory with ScalaFutures {

  private trait Fixture {
    val TargetBaseTable = "unit_link"
    val TargetPeriod = Period.fromYearMonth(2018, AUGUST)
    val TargetPeriodTable = s"${TargetBaseTable}_${Period.asString(TargetPeriod)}"
    val TargetUnitId = UnitId("1234567890123456")
    val TargetUnitType = LegalUnit
    val TargetRowKey = UnitLinksRowKey(TargetUnitId, TargetUnitType)
    val TargetUnitKey = UnitKey(TargetUnitId, TargetUnitType, TargetPeriod)
    val DummyRow = Row(TargetRowKey, Map.empty)

    val restRepository = mock[RestRepository]
    val config = HBaseRestUnitLinksRepositoryConfig(TargetBaseTable)
    val registerService = new UnitLinkUnitRegisterService(restRepository, config)(ExecutionContext.global)
  }

  "A UnitLink UnitRegisterService" - {
    "returns UnitFound when the target unit is known to the register (via unit links)" in new Fixture {
      (restRepository.findRow _).expects(TargetPeriodTable, TargetRowKey, ColumnFamily).returning(
        Future.successful(Right(Some(DummyRow)))
      )

      whenReady(registerService.isRegisteredUnit(TargetUnitKey)) { result =>
        result shouldBe UnitFound
      }
    }

    "returns UnitNotFound when the target unit is not known to the register (via unit links)" in new Fixture {
      (restRepository.findRow _).expects(TargetPeriodTable, TargetRowKey, ColumnFamily).returning(
        Future.successful(Right(None))
      )

      whenReady(registerService.isRegisteredUnit(TargetUnitKey)) { result =>
        result shouldBe UnitNotFound
      }
    }

    "returns UnitRegisterFailure when an error is encountered while querying the register" in new Fixture {
      val failureMessage = "operation failed"
      (restRepository.findRow _).expects(TargetPeriodTable, TargetRowKey, ColumnFamily).returning(
        Future.successful(Left(failureMessage))
      )

      whenReady(registerService.isRegisteredUnit(TargetUnitKey)) { result =>
        result shouldBe UnitRegisterFailure(failureMessage)
      }
    }
  }
}
