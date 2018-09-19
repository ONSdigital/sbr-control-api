package services

import java.time.Month.MARCH

import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ FreeSpec, Matchers }
import play.api.libs.json.{ JsNumber, JsString }
import repository._
import uk.gov.ons.sbr.models.patch.{ AddOperation, RemoveOperation, ReplaceOperation, TestOperation }
import uk.gov.ons.sbr.models.unitlinks.UnitId
import uk.gov.ons.sbr.models.unitlinks.UnitType.{ CompaniesHouse, LegalUnit, PayAsYouEarnTax, ValueAddedTax, toAcronym }
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
    val OtherVatRef = "987654321012"

    val repository = mock[UnitLinksRepository]
    val vatRegisterService = mock[UnitRegisterService]
    val patchService = new LegalUnitLinkPatchService(repository, vatRegisterService)
  }

  private trait CreateFixture extends Fixture {
    val CreateChildPatch = Seq(AddOperation(s"/children/$VatRef", JsString(toAcronym(ValueAddedTax))))
  }

  private trait DeleteFixture extends Fixture {
    val DeleteChildPatch = Seq(
      TestOperation(s"/children/$VatRef", JsString(toAcronym(ValueAddedTax))),
      RemoveOperation(s"/children/$VatRef")
    )
  }

  "A Legal Unit Link PatchService" - {
    "can process a patch specifying that a child VAT unit should be created" - {
      "signalling success when the patch is successfully applied" in new CreateFixture {
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

      "signalling when the target Legal Unit cannot be found" in new CreateFixture {
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

      "signalling when the application of the patch fails" in new CreateFixture {
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

      "signalling failure when an error is encountered while attempting to confirm that the target VAT reference is known to the Admin Data" in new CreateFixture {
        (vatRegisterService.isRegisteredUnit _).expects(VatUnitKey).returning(
          Future.successful(UnitRegisterFailure("operation failed"))
        )

        whenReady(patchService.applyPatchTo(LegalUnitKey, CreateChildPatch)) { result =>
          result shouldBe PatchFailed
        }
      }

      "rejecting" - {
        "a patch specifying a target VAT reference that does not exist in the admin data" in new CreateFixture {
          (vatRegisterService.isRegisteredUnit _).expects(VatUnitKey).returning(
            Future.successful(UnitNotFound)
          )

          whenReady(patchService.applyPatchTo(LegalUnitKey, CreateChildPatch)) { result =>
            result shouldBe PatchRejected
          }
        }

        "a patch that attempts to add multiple values" in new Fixture {
          val invalidPatch = Seq(
            AddOperation(s"/children/$VatRef", JsString(toAcronym(ValueAddedTax))),
            AddOperation("/children/210987654321", JsString(toAcronym(ValueAddedTax)))
          )

          whenReady(patchService.applyPatchTo(LegalUnitKey, invalidPatch)) { result =>
            result shouldBe PatchRejected
          }
        }

        "a patch that attempts to add a path other than '/children/<some-id>'" in new Fixture {
          val invalidPathPatch = Seq(AddOperation(s"/child/$VatRef", JsString(toAcronym(ValueAddedTax))))

          whenReady(patchService.applyPatchTo(LegalUnitKey, invalidPathPatch)) { result =>
            result shouldBe PatchRejected
          }
        }

        "a patch containing a value that is not a string (and so cannot be a unit type acronym)" in new Fixture {
          val invalidValuePatch = Seq(AddOperation(s"/children/$VatRef", JsNumber(42)))

          whenReady(patchService.applyPatchTo(LegalUnitKey, invalidValuePatch)) { result =>
            result shouldBe PatchRejected
          }
        }

        "a patch containing a value that is not the acronym for the VAT unit type" in new Fixture {
          val invalidValuePatch = Seq(AddOperation(s"/children/$VatRef", JsString(toAcronym(LegalUnit))))

          whenReady(patchService.applyPatchTo(LegalUnitKey, invalidValuePatch)) { result =>
            result shouldBe PatchRejected
          }
        }
      }
    }

    "can process a patch specifying that a child VAT unit should be deleted" - {
      "signalling success when the patch is successfully applied" in new DeleteFixture {
        (repository.deleteChildLink _).expects(LegalUnitKey, ValueAddedTax, VatUnitId).returning(
          Future.successful(EditApplied)
        )

        whenReady(patchService.applyPatchTo(LegalUnitKey, DeleteChildPatch)) { result =>
          result shouldBe PatchApplied
        }
      }

      "signalling when a patch cannot be applied as a result of a conflicting change" in new DeleteFixture {
        (repository.deleteChildLink _).expects(LegalUnitKey, ValueAddedTax, VatUnitId).returning(
          Future.successful(EditConflicted)
        )

        whenReady(patchService.applyPatchTo(LegalUnitKey, DeleteChildPatch)) { result =>
          result shouldBe PatchConflicted
        }
      }

      "signalling when the target Legal Unit cannot be found" in new DeleteFixture {
        (repository.deleteChildLink _).expects(LegalUnitKey, ValueAddedTax, VatUnitId).returning(
          Future.successful(EditTargetNotFound)
        )

        whenReady(patchService.applyPatchTo(LegalUnitKey, DeleteChildPatch)) { result =>
          result shouldBe PatchTargetNotFound
        }
      }

      "signalling when the application of the patch fails" in new DeleteFixture {
        (repository.deleteChildLink _).expects(LegalUnitKey, ValueAddedTax, VatUnitId).returning(
          Future.successful(EditFailed)
        )

        whenReady(patchService.applyPatchTo(LegalUnitKey, DeleteChildPatch)) { result =>
          result shouldBe PatchFailed
        }
      }

      "rejecting" - {
        /*
         * Permitting an outright 'remove' operation would not allow us to use a 'checkAndDelete' operation,
         * and would run the risk of overwriting another user's concurrent changes.
         */
        "a patch that does not pair a 'test' operation with the 'remove' operation" in new DeleteFixture {
          whenReady(patchService.applyPatchTo(LegalUnitKey, Seq(RemoveOperation(s"/children/$VatRef")))) { result =>
            result shouldBe PatchRejected
          }
        }

        "a patch that attempts to remove multiple values" in new DeleteFixture {
          val invalidPatch = Seq(
            TestOperation(s"/children/$VatRef", JsString(toAcronym(ValueAddedTax))),
            RemoveOperation(s"/children/$VatRef"),
            RemoveOperation(s"/children/$OtherVatRef")
          )

          whenReady(patchService.applyPatchTo(LegalUnitKey, invalidPatch)) { result =>
            result shouldBe PatchRejected
          }
        }

        "a patch where the paths for the 'test' and 'remove' operations differ" in new DeleteFixture {
          val invalidPatch = Seq(
            TestOperation(s"/children/$VatRef", JsString(toAcronym(ValueAddedTax))),
            RemoveOperation(s"/children/$OtherVatRef")
          )

          whenReady(patchService.applyPatchTo(LegalUnitKey, invalidPatch)) { result =>
            result shouldBe PatchRejected
          }
        }

        "a patch that attempts to delete a path other than '/children/<some-id>'" in new DeleteFixture {
          val invalidPatch = Seq(
            TestOperation(s"/child/$VatRef", JsString(toAcronym(ValueAddedTax))),
            RemoveOperation(s"/child/$VatRef")
          )

          whenReady(patchService.applyPatchTo(LegalUnitKey, invalidPatch)) { result =>
            result shouldBe PatchRejected
          }
        }

        "a patch containing a 'test' value that is not a string (and so cannot be a unit type acronym)" in new Fixture {
          val invalidValuePatch = Seq(
            TestOperation(s"/children/$VatRef", JsNumber(42)),
            RemoveOperation(s"/children/$VatRef")
          )

          whenReady(patchService.applyPatchTo(LegalUnitKey, invalidValuePatch)) { result =>
            result shouldBe PatchRejected
          }
        }

        "a patch containing a 'test' value that is not the acronym for the VAT unit type" in new Fixture {
          val invalidValuePatch = Seq(
            TestOperation(s"/children/$VatRef", JsString(toAcronym(CompaniesHouse))),
            RemoveOperation(s"/children/$VatRef")
          )

          whenReady(patchService.applyPatchTo(LegalUnitKey, invalidValuePatch)) { result =>
            result shouldBe PatchRejected
          }
        }
      }
    }

    "rejects a patch that does not represent either an add operation or a delete operation" in new Fixture {
      val updatePatch = Seq(
        TestOperation(s"/children/$VatRef", JsString(toAcronym(ValueAddedTax))),
        ReplaceOperation(s"/children/$VatRef", JsString(toAcronym(PayAsYouEarnTax)))
      )

      whenReady(patchService.applyPatchTo(LegalUnitKey, updatePatch)) { result =>
        result shouldBe PatchRejected
      }
    }
  }
}
