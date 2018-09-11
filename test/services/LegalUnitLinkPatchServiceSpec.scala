package services

import java.time.Month.MARCH

import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ FreeSpec, Matchers }
import play.api.libs.json.{ JsNumber, JsString }
import repository.{ CreateChildLinkFailure, CreateChildLinkSuccess, LinkFromUnitNotFound, UnitLinksRepository }
import uk.gov.ons.sbr.models.patch.Operation
import uk.gov.ons.sbr.models.patch.OperationTypes.{ Add, Test }
import uk.gov.ons.sbr.models.unitlinks.UnitId
import uk.gov.ons.sbr.models.unitlinks.UnitType.{ LegalUnit, ValueAddedTax, toAcronym }
import uk.gov.ons.sbr.models.{ Period, UnitKey }

import scala.concurrent.Future

class LegalUnitLinkPatchServiceSpec extends FreeSpec with Matchers with MockFactory with ScalaFutures {

  private trait Fixture {
    val LegalUnitId = UnitId("9999999999999999")
    val RegisterPeriod = Period.fromYearMonth(2018, MARCH)
    val LegalUnitKey = UnitKey(LegalUnitId, LegalUnit, RegisterPeriod)
    val VatRef = "123456789012"
    val VatUnitId = UnitId(VatRef)
    val VatUnitKey = UnitKey(VatUnitId, ValueAddedTax, RegisterPeriod)
    val CreateChildPatch = Seq(Operation(Add, s"/children/$VatRef", JsString(toAcronym(ValueAddedTax))))

    val repository = mock[UnitLinksRepository]
    val vatRegisterService = mock[UnitRegisterService]
    val patchService = new LegalUnitLinkPatchService(repository, vatRegisterService)
  }

  "A Legal Unit Link PatchService" - {
    "can apply a patch that specifies that a link to a child VAT unit should be created" in new Fixture {
      (vatRegisterService.isRegisteredUnit _).expects(VatUnitKey).returning(
        Future.successful(UnitFound)
      )
      (repository.createChildLink _).expects(LegalUnitKey, ValueAddedTax, VatUnitId).returning(
        Future.successful(CreateChildLinkSuccess)
      )

      whenReady(patchService.applyPatchTo(LegalUnitKey, CreateChildPatch)) { result =>
        result shouldBe PatchApplied
      }
    }

    "can signal that the target Legal Unit could not be found" in new Fixture {
      (vatRegisterService.isRegisteredUnit _).expects(VatUnitKey).returning(
        Future.successful(UnitFound)
      )
      (repository.createChildLink _).expects(LegalUnitKey, ValueAddedTax, VatUnitId).returning(
        Future.successful(LinkFromUnitNotFound)
      )

      whenReady(patchService.applyPatchTo(LegalUnitKey, CreateChildPatch)) { result =>
        result shouldBe PatchTargetNotFound
      }
    }

    "can signal that a patch was accepted and attempted, but that the operation failed" in new Fixture {
      (vatRegisterService.isRegisteredUnit _).expects(VatUnitKey).returning(
        Future.successful(UnitFound)
      )
      (repository.createChildLink _).expects(LegalUnitKey, ValueAddedTax, VatUnitId).returning(
        Future.successful(CreateChildLinkFailure)
      )

      whenReady(patchService.applyPatchTo(LegalUnitKey, CreateChildPatch)) { result =>
        result shouldBe PatchFailed
      }
    }

    "rejects a patch specifying a target VAT reference that does not exist in the admin data" in new Fixture {
      (vatRegisterService.isRegisteredUnit _).expects(VatUnitKey).returning(
        Future.successful(UnitNotFound)
      )

      whenReady(patchService.applyPatchTo(LegalUnitKey, CreateChildPatch)) { result =>
        result shouldBe PatchRejected
      }
    }

    "rejects a patch that is not an add operation" in new Fixture {
      val invalidPatch = Seq(Operation(Test, s"/children/$VatRef", JsString(toAcronym(ValueAddedTax))))

      whenReady(patchService.applyPatchTo(LegalUnitKey, invalidPatch)) { result =>
        result shouldBe PatchRejected
      }
    }

    "rejects a patch that is not a single operation" in new Fixture {
      val invalidPatch = Seq(
        Operation(Add, s"/children/$VatRef", JsString(toAcronym(ValueAddedTax))),
        Operation(Add, "/children/210987654321", JsString(toAcronym(ValueAddedTax)))
      )

      whenReady(patchService.applyPatchTo(LegalUnitKey, invalidPatch)) { result =>
        result shouldBe PatchRejected
      }
    }

    "rejects a patch for any path other than '/children/<some-id>'" in new Fixture {
      val invalidPathPatch = Seq(Operation(Add, s"/child/$VatRef", JsString(toAcronym(ValueAddedTax))))

      whenReady(patchService.applyPatchTo(LegalUnitKey, invalidPathPatch)) { result =>
        result shouldBe PatchRejected
      }
    }

    "rejects a patch containing a value that is not a string and so cannot be a unit type acronym" in new Fixture {
      val invalidValuePatch = Seq(Operation(Add, s"/children/$VatRef", JsNumber(42)))

      whenReady(patchService.applyPatchTo(LegalUnitKey, invalidValuePatch)) { result =>
        result shouldBe PatchRejected
      }
    }

    "rejects a patch containing a value that is not the acronym for the VAT unit type" in new Fixture {
      val invalidValuePatch = Seq(Operation(Add, s"/children/$VatRef", JsString(toAcronym(LegalUnit))))

      whenReady(patchService.applyPatchTo(LegalUnitKey, invalidValuePatch)) { result =>
        result shouldBe PatchRejected
      }
    }

    "fails when an error is encountered while attempting to confirm that the target VAT reference is known to the Admin Data" in new Fixture {
      (vatRegisterService.isRegisteredUnit _).expects(VatUnitKey).returning(
        Future.successful(UnitRegisterFailure("operation failed"))
      )

      whenReady(patchService.applyPatchTo(LegalUnitKey, CreateChildPatch)) { result =>
        result shouldBe PatchFailed
      }
    }
  }
}
