package services

import java.time.Month.MARCH

import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ FreeSpec, Matchers }
import play.api.libs.json.{ JsNumber, JsString }
import repository._
import uk.gov.ons.sbr.models.patch.{ ReplaceOperation, TestOperation }
import uk.gov.ons.sbr.models.unitlinks.UnitId
import uk.gov.ons.sbr.models.unitlinks.UnitType.{ LegalUnit, ValueAddedTax }
import uk.gov.ons.sbr.models.{ Period, UnitKey }

import scala.concurrent.Future

class AdminUnitLinkPatchServiceSpec extends FreeSpec with Matchers with MockFactory with ScalaFutures {

  private trait Fixture {
    val AdminUnitId = UnitId("123456789012")
    val RegisterPeriod = Period.fromYearMonth(2018, MARCH)
    val IncorrectLegalUnitId = UnitId("1111111111111111")
    val TargetLegalUnitId = UnitId("9999999999999999")
    val TargetLegalUnitKey = UnitKey(TargetLegalUnitId, LegalUnit, RegisterPeriod)
    val TargetAdminUnitKey = UnitKey(AdminUnitId, ValueAddedTax, RegisterPeriod)
    val UpdateDescriptor = UpdateParentDescriptor(LegalUnit, IncorrectLegalUnitId, TargetLegalUnitId)
    val UpdateParentPatch = Seq(
      TestOperation("/parents/LEU", JsString(IncorrectLegalUnitId.value)),
      ReplaceOperation("/parents/LEU", JsString(TargetLegalUnitId.value))
    )

    val repository = mock[UnitLinksRepository]
    val unitRegisterService = mock[UnitRegisterService]
    val service = new AdminUnitLinkPatchService(repository, unitRegisterService)
  }

  "An admin data Unit Link PatchService" - {
    "can apply a patch that specifies a checked update of an admin unit's parent UBRN to a Legal Unit that already exists in the register" in new Fixture {
      (unitRegisterService.isRegisteredUnit _).expects(TargetLegalUnitKey).returning(
        Future.successful(UnitFound)
      )
      (repository.updateParentLink _).expects(TargetAdminUnitKey, UpdateDescriptor).returning(
        Future.successful(EditApplied)
      )

      whenReady(service.applyPatchTo(TargetAdminUnitKey, UpdateParentPatch)) { result =>
        result shouldBe PatchApplied
      }
    }

    "can signal that a patch was considered valid but could not be applied as a result of a conflicting change" in new Fixture {
      (unitRegisterService.isRegisteredUnit _).expects(TargetLegalUnitKey).returning(
        Future.successful(UnitFound)
      )
      (repository.updateParentLink _).expects(TargetAdminUnitKey, UpdateDescriptor).returning(
        Future.successful(EditConflicted)
      )

      whenReady(service.applyPatchTo(TargetAdminUnitKey, UpdateParentPatch)) { result =>
        result shouldBe PatchConflicted
      }
    }

    "can signal that the target entity of the patch could not be found" in new Fixture {
      (unitRegisterService.isRegisteredUnit _).expects(TargetLegalUnitKey).returning(
        Future.successful(UnitFound)
      )
      (repository.updateParentLink _).expects(TargetAdminUnitKey, UpdateDescriptor).returning(
        Future.successful(EditTargetNotFound)
      )

      whenReady(service.applyPatchTo(TargetAdminUnitKey, UpdateParentPatch)) { result =>
        result shouldBe PatchTargetNotFound
      }
    }

    "can signal that a patch was accepted and submitted for update, but that the operation failed" in new Fixture {
      (unitRegisterService.isRegisteredUnit _).expects(TargetLegalUnitKey).returning(
        Future.successful(UnitFound)
      )
      (repository.updateParentLink _).expects(TargetAdminUnitKey, UpdateDescriptor).returning(
        Future.successful(EditFailed)
      )

      whenReady(service.applyPatchTo(TargetAdminUnitKey, UpdateParentPatch)) { result =>
        result shouldBe PatchFailed
      }
    }

    "rejects a patch for any path other than '/parents/LEU'" in new Fixture {
      val invalidPathPatch = Seq(
        TestOperation("/parents/ENT", JsString("test-ern")),
        ReplaceOperation("/parents/ENT", JsString("replace-ern"))
      )

      whenReady(service.applyPatchTo(TargetAdminUnitKey, invalidPathPatch)) { result =>
        result shouldBe PatchRejected
      }
    }

    /*
     * Permitting a 'replace' operation by itself would not allow us to use a 'checkAndUpdate' operation,
     * and would run the risk of overwriting another user's concurrent changes.
     */
    "rejects a patch that does not contain both a 'test' and a 'replace' operation" in new Fixture {
      val noTestPatch = Seq(
        ReplaceOperation("/parents/LEU", JsString(TargetLegalUnitId.value))
      )

      whenReady(service.applyPatchTo(TargetAdminUnitKey, noTestPatch)) { result =>
        result shouldBe PatchRejected
      }
    }

    "rejects a patch containing a test value that is not a string and so cannot be a UBRN" in new Fixture {
      val invalidUbrnPatch = Seq(
        TestOperation("/parents/LEU", JsNumber(42)),
        ReplaceOperation("/parents/LEU", JsString(TargetLegalUnitId.value))
      )

      whenReady(service.applyPatchTo(TargetAdminUnitKey, invalidUbrnPatch)) { result =>
        result shouldBe PatchRejected
      }
    }

    "rejects a patch containing a replacement value that is not a string and so cannot be a UBRN" in new Fixture {
      val invalidUbrnPatch = Seq(
        TestOperation("/parents/LEU", JsString(IncorrectLegalUnitId.value)),
        ReplaceOperation("/parents/LEU", JsNumber(42))
      )

      whenReady(service.applyPatchTo(TargetAdminUnitKey, invalidUbrnPatch)) { result =>
        result shouldBe PatchRejected
      }
    }

    "rejects a patch specifying a target UBRN that does not already exist in the register" in new Fixture {
      (unitRegisterService.isRegisteredUnit _).expects(TargetLegalUnitKey).returning(
        Future.successful(UnitNotFound)
      )

      whenReady(service.applyPatchTo(TargetAdminUnitKey, UpdateParentPatch)) { result =>
        result shouldBe PatchRejected
      }
    }

    "fails when an error is encountered while attempting to confirm that the target UBRN already exists in the register" in new Fixture {
      (unitRegisterService.isRegisteredUnit _).expects(TargetLegalUnitKey).returning(
        Future.successful(UnitRegisterFailure("some error messsage"))
      )

      whenReady(service.applyPatchTo(TargetAdminUnitKey, UpdateParentPatch)) { result =>
        result shouldBe PatchFailed
      }
    }
  }
}
