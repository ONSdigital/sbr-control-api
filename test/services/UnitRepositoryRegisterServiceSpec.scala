package services

import java.time.Month.AUGUST

import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ FreeSpec, Matchers }
import repository.UnitLinksRepository
import uk.gov.ons.sbr.models.unitlinks.UnitType.{ Enterprise, LegalUnit }
import uk.gov.ons.sbr.models.unitlinks.{ UnitId, UnitLinks }
import uk.gov.ons.sbr.models.{ Period, UnitKey }

import scala.concurrent.Future

class UnitRepositoryRegisterServiceSpec extends FreeSpec with Matchers with MockFactory with ScalaFutures {

  private trait Fixture {
    val TargetUnitId = UnitId("1234567890123456")
    val TargetUnitType = LegalUnit
    val TargetPeriod = Period.fromYearMonth(2018, AUGUST)
    val TargetUnitKey = UnitKey(TargetUnitId, TargetUnitType, TargetPeriod)
    val FoundUnitLinks = UnitLinks(id = TargetUnitId, unitType = TargetUnitType, period = TargetPeriod,
      parents = Some(Map(Enterprise -> UnitId("9876543210"))), children = None)

    val unitLinksRepository = mock[UnitLinksRepository]
    val registerService = new UnitRepositoryRegisterService(unitLinksRepository)
  }

  "A UnitRepository RegisterService" - {
    "returns UnitFound when the target unit is known to the register" in new Fixture {
      (unitLinksRepository.retrieveUnitLinks _).expects(TargetUnitKey).returning(
        Future.successful(Right(Some(FoundUnitLinks)))
      )

      whenReady(registerService.isRegisteredUnit(TargetUnitKey)) { result =>
        result shouldBe UnitFound
      }
    }

    "returns UnitNotFound when the target unit is not known to the register" in new Fixture {
      (unitLinksRepository.retrieveUnitLinks _).expects(TargetUnitKey).returning(
        Future.successful(Right(None))
      )

      whenReady(registerService.isRegisteredUnit(TargetUnitKey)) { result =>
        result shouldBe UnitNotFound
      }
    }

    "returns UnitRegisterFailure when an error is encountered while querying the register" in new Fixture {
      val failureMessage = "operation failed"
      (unitLinksRepository.retrieveUnitLinks _).expects(TargetUnitKey).returning(
        Future.successful(Left(failureMessage))
      )

      whenReady(registerService.isRegisteredUnit(TargetUnitKey)) { result =>
        result shouldBe UnitRegisterFailure(failureMessage)
      }
    }
  }
}
