package handlers

import java.time.Month.AUGUST

import handlers.http.HttpPatchHandler
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FreeSpec, Matchers}
import play.api.mvc.Results.{Conflict, InternalServerError, NoContent, NotFound, UnprocessableEntity}
import services._
import uk.gov.ons.sbr.models.unitlinks.UnitId
import uk.gov.ons.sbr.models.unitlinks.UnitType.LegalUnit
import uk.gov.ons.sbr.models.{Period, UnitKey}

import scala.concurrent.{ExecutionContext, Future}

class HttpPatchHandlerSpec extends FreeSpec with Matchers with MockFactory with ScalaFutures {

  private trait Fixture {
    val TargetUnitKey = UnitKey(UnitId("1234567890123456"), LegalUnit, Period.fromYearMonth(2018, AUGUST))
    val TargetPatch = Seq.empty

    val patchService = mock[PatchService]
    val patchHandler = new HttpPatchHandler(patchService)(ExecutionContext.global)
  }

  "A Http PatchHandler" - {
    "returns NoContent when application of a patch succeeds" in new Fixture {
      (patchService.applyPatchTo _).expects(TargetUnitKey, TargetPatch).returning(
        Future.successful(PatchApplied)
      )

      whenReady(patchHandler.apply(TargetUnitKey, TargetPatch)) { result =>
        result shouldBe NoContent
      }
    }

    "returns Conflict when a patch conflicts with another edit" in new Fixture {
      (patchService.applyPatchTo _).expects(TargetUnitKey, TargetPatch).returning(
        Future.successful(PatchConflicted)
      )

      whenReady(patchHandler.apply(TargetUnitKey, TargetPatch)) { result =>
        result shouldBe Conflict
      }
    }

    "returns NotFound when the patch target cannot be found" in new Fixture {
      (patchService.applyPatchTo _).expects(TargetUnitKey, TargetPatch).returning(
        Future.successful(PatchTargetNotFound)
      )

      whenReady(patchHandler.apply(TargetUnitKey, TargetPatch)) { result =>
        result shouldBe NotFound
      }
    }

    "returns UnprocessableEntity when a patch is rejected" in new Fixture {
      (patchService.applyPatchTo _).expects(TargetUnitKey, TargetPatch).returning(
        Future.successful(PatchRejected)
      )

      whenReady(patchHandler.apply(TargetUnitKey, TargetPatch)) { result =>
        result shouldBe UnprocessableEntity
      }
    }

    "returns InternalServerError when the application of a patch fails" in new Fixture {
      (patchService.applyPatchTo _).expects(TargetUnitKey, TargetPatch).returning(
        Future.successful(PatchFailed)
      )

      whenReady(patchHandler.apply(TargetUnitKey, TargetPatch)) { result =>
        result shouldBe InternalServerError
      }
    }
  }
}
