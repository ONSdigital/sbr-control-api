package services

import java.time.Month.MARCH

import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ FreeSpec, Matchers }
import play.api.libs.json.{ JsNumber, JsString }
import repository._
import uk.gov.ons.sbr.models.patch.{ AddOperation, RemoveOperation, ReplaceOperation, TestOperation }
import uk.gov.ons.sbr.models.unitlinks.UnitId
import uk.gov.ons.sbr.models.unitlinks.UnitType.{ CompaniesHouse, LegalUnit, PayAsYouEarn, ValueAddedTax, toAcronym }
import uk.gov.ons.sbr.models.{ Period, UnitKey }

import scala.concurrent.Future

class LegalUnitLinkPatchServiceSpec extends FreeSpec with Matchers with MockFactory with ScalaFutures {

  private trait Fixture {
    val RegisterPeriod = Period.fromYearMonth(2018, MARCH)
    val LegalUnitId = UnitId("9999999999999999")
    val LegalUnitKey = UnitKey(LegalUnitId, LegalUnit, RegisterPeriod)
    val OtherRef = "987654321012"

    val repository = mock[UnitLinksRepository]
    val unitRegisterService = mock[UnitRegisterService]
    val patchService = new LegalUnitLinkPatchService(repository, unitRegisterService)
  }

  private trait VatFixture extends Fixture {
    val VatRef = "123456789012"
    val VatUnitId = UnitId(VatRef)
    val VatUnitKey = UnitKey(VatUnitId, ValueAddedTax, RegisterPeriod)
  }

  private trait PayeFixture extends Fixture {
    val PayeRef = "A1B2C3D4"
    val PayeUnitId = UnitId(PayeRef)
    val PayeUnitKey = UnitKey(PayeUnitId, PayAsYouEarn, RegisterPeriod)
  }

  private trait CreateVatFixture extends VatFixture {
    val CreateVatChildPatch = Seq(AddOperation(s"/children/$VatRef", JsString(toAcronym(ValueAddedTax))))
  }

  private trait CreatePayeFixture extends PayeFixture {
    val CreatePayeChildPatch = Seq(AddOperation(s"/children/$PayeRef", JsString(toAcronym(PayAsYouEarn))))
  }

  private trait DeleteVatFixture extends VatFixture {
    val DeleteVatChildPatch = Seq(
      TestOperation(s"/children/$VatRef", JsString(toAcronym(ValueAddedTax))),
      RemoveOperation(s"/children/$VatRef")
    )
  }

  private trait DeletePayeFixture extends PayeFixture {
    val DeletePayeChildPatch = Seq(
      TestOperation(s"/children/$PayeRef", JsString(toAcronym(PayAsYouEarn))),
      RemoveOperation(s"/children/$PayeRef")
    )
  }

  "A Legal Unit Link PatchService" - {
    "can process a patch specifying that a child unit link should be created" - {
      "with unit type VAT" - {
        "signalling success when the patch is successfully applied" in new CreateVatFixture {
          (unitRegisterService.isRegisteredUnit _).expects(VatUnitKey).returning(
            Future.successful(UnitFound)
          )
          (repository.createChildLink _).expects(LegalUnitKey, ValueAddedTax, VatUnitId).returning(
            Future.successful(CreateChildLinkSuccess)
          )

          whenReady(patchService.applyPatchTo(LegalUnitKey, CreateVatChildPatch)) { result =>
            result shouldBe PatchApplied
          }
        }
      }

      "with unit type PAYE" - {
        "signalling success when the patch is successfully applied" in new CreatePayeFixture {
          (unitRegisterService.isRegisteredUnit _).expects(PayeUnitKey).returning(
            Future.successful(UnitFound)
          )
          (repository.createChildLink _).expects(LegalUnitKey, PayAsYouEarn, PayeUnitId).returning(
            Future.successful(CreateChildLinkSuccess)
          )

          whenReady(patchService.applyPatchTo(LegalUnitKey, CreatePayeChildPatch)) { result =>
            result shouldBe PatchApplied
          }
        }
      }

      "signalling when the target Legal Unit cannot be found" in new CreateVatFixture {
        (unitRegisterService.isRegisteredUnit _).expects(VatUnitKey).returning(
          Future.successful(UnitFound)
        )
        (repository.createChildLink _).expects(LegalUnitKey, ValueAddedTax, VatUnitId).returning(
          Future.successful(LinkFromUnitNotFound)
        )

        whenReady(patchService.applyPatchTo(LegalUnitKey, CreateVatChildPatch)) { result =>
          result shouldBe PatchTargetNotFound
        }
      }

      "signalling when the application of the patch fails" in new CreatePayeFixture {
        (unitRegisterService.isRegisteredUnit _).expects(PayeUnitKey).returning(
          Future.successful(UnitFound)
        )
        (repository.createChildLink _).expects(LegalUnitKey, PayAsYouEarn, PayeUnitId).returning(
          Future.successful(CreateChildLinkFailure)
        )

        whenReady(patchService.applyPatchTo(LegalUnitKey, CreatePayeChildPatch)) { result =>
          result shouldBe PatchFailed
        }
      }

      "signalling failure when an error is encountered while attempting to confirm that the target child reference is known to the Admin Data" in new CreateVatFixture {
        (unitRegisterService.isRegisteredUnit _).expects(VatUnitKey).returning(
          Future.successful(UnitRegisterFailure("operation failed"))
        )

        whenReady(patchService.applyPatchTo(LegalUnitKey, CreateVatChildPatch)) { result =>
          result shouldBe PatchFailed
        }
      }

      "rejecting" - {
        "a patch specifying a target child reference that does not exist in the admin data" in new CreatePayeFixture {
          (unitRegisterService.isRegisteredUnit _).expects(PayeUnitKey).returning(
            Future.successful(UnitNotFound)
          )

          whenReady(patchService.applyPatchTo(LegalUnitKey, CreatePayeChildPatch)) { result =>
            result shouldBe PatchRejected
          }
        }

        "a patch that attempts to add multiple values" in new VatFixture {
          val invalidPatch = Seq(
            AddOperation(s"/children/$VatRef", JsString(toAcronym(ValueAddedTax))),
            AddOperation("/children/210987654321", JsString(toAcronym(ValueAddedTax)))
          )

          whenReady(patchService.applyPatchTo(LegalUnitKey, invalidPatch)) { result =>
            result shouldBe PatchRejected
          }
        }

        "a patch that attempts to add a path other than '/children/<some-id>'" in new PayeFixture {
          val invalidPathPatch = Seq(AddOperation(s"/child/$PayeRef", JsString(toAcronym(PayAsYouEarn))))

          whenReady(patchService.applyPatchTo(LegalUnitKey, invalidPathPatch)) { result =>
            result shouldBe PatchRejected
          }
        }

        "a patch containing a value that is not a string (and so cannot be a unit type acronym)" in new Fixture {
          val invalidValuePatch = Seq(AddOperation(s"/children/childref", JsNumber(42)))

          whenReady(patchService.applyPatchTo(LegalUnitKey, invalidValuePatch)) { result =>
            result shouldBe PatchRejected
          }
        }

        "a patch containing a value that is not the acronym for either the VAT or PAYE unit type" in new Fixture {
          val invalidValuePatch = Seq(AddOperation(s"/children/childref", JsString(toAcronym(CompaniesHouse))))

          whenReady(patchService.applyPatchTo(LegalUnitKey, invalidValuePatch)) { result =>
            result shouldBe PatchRejected
          }
        }
      }
    }

    "can process a patch specifying that a child unit link should be deleted" - {
      "with unit type VAT" - {
        "signalling success when the patch is successfully applied" in new DeleteVatFixture {
          (repository.deleteChildLink _).expects(LegalUnitKey, ValueAddedTax, VatUnitId).returning(
            Future.successful(EditApplied)
          )

          whenReady(patchService.applyPatchTo(LegalUnitKey, DeleteVatChildPatch)) { result =>
            result shouldBe PatchApplied
          }
        }

        "rejecting" - {
          "a patch that attempts to remove multiple values" in new DeleteVatFixture {
            val invalidPatch = Seq(
              TestOperation(s"/children/$VatRef", JsString(toAcronym(ValueAddedTax))),
              RemoveOperation(s"/children/$VatRef"),
              RemoveOperation(s"/children/$OtherRef")
            )

            whenReady(patchService.applyPatchTo(LegalUnitKey, invalidPatch)) { result =>
              result shouldBe PatchRejected
            }
          }

          "a patch that contains multiple test operations (even if they represent the same test)" in new DeleteVatFixture {
            val invalidPatch = Seq(
              TestOperation(s"/children/$VatRef", JsString(toAcronym(ValueAddedTax))),
              TestOperation(s"/children/$VatRef", JsString(toAcronym(ValueAddedTax))),
              RemoveOperation(s"/children/$VatRef")
            )

            whenReady(patchService.applyPatchTo(LegalUnitKey, invalidPatch)) { result =>
              result shouldBe PatchRejected
            }
          }

          "a patch where the paths for the 'test' and 'remove' operations differ" in new DeleteVatFixture {
            val invalidPatch = Seq(
              TestOperation(s"/children/$VatRef", JsString(toAcronym(ValueAddedTax))),
              RemoveOperation(s"/children/$OtherRef")
            )

            whenReady(patchService.applyPatchTo(LegalUnitKey, invalidPatch)) { result =>
              result shouldBe PatchRejected
            }
          }

          "a patch containing a 'test' value that is not a string (and so cannot be a unit type acronym)" in new VatFixture {
            val invalidValuePatch = Seq(
              TestOperation(s"/children/$VatRef", JsNumber(42)),
              RemoveOperation(s"/children/$VatRef")
            )

            whenReady(patchService.applyPatchTo(LegalUnitKey, invalidValuePatch)) { result =>
              result shouldBe PatchRejected
            }
          }
        }
      }

      "with unit type PAYE" - {
        "signalling success when the patch is successfully applied" in new DeletePayeFixture {
          (repository.deleteChildLink _).expects(LegalUnitKey, PayAsYouEarn, PayeUnitId).returning(
            Future.successful(EditApplied)
          )

          whenReady(patchService.applyPatchTo(LegalUnitKey, DeletePayeChildPatch)) { result =>
            result shouldBe PatchApplied
          }
        }

        "rejecting" - {
          "a patch that attempts to remove multiple values" in new DeletePayeFixture {
            val invalidPatch = Seq(
              TestOperation(s"/children/$PayeRef", JsString(toAcronym(PayAsYouEarn))),
              RemoveOperation(s"/children/$PayeRef"),
              RemoveOperation(s"/children/$OtherRef")
            )

            whenReady(patchService.applyPatchTo(LegalUnitKey, invalidPatch)) { result =>
              result shouldBe PatchRejected
            }
          }

          "a patch that contains multiple test operations (even if they represent the same test)" in new DeletePayeFixture {
            val invalidPatch = Seq(
              TestOperation(s"/children/$PayeRef", JsString(toAcronym(PayAsYouEarn))),
              TestOperation(s"/children/$PayeRef", JsString(toAcronym(PayAsYouEarn))),
              RemoveOperation(s"/children/$PayeRef")
            )

            whenReady(patchService.applyPatchTo(LegalUnitKey, invalidPatch)) { result =>
              result shouldBe PatchRejected
            }
          }

          "a patch where the paths for the 'test' and 'remove' operations differ" in new DeletePayeFixture {
            val invalidPatch = Seq(
              TestOperation(s"/children/$PayeRef", JsString(toAcronym(PayAsYouEarn))),
              RemoveOperation(s"/children/$OtherRef")
            )

            whenReady(patchService.applyPatchTo(LegalUnitKey, invalidPatch)) { result =>
              result shouldBe PatchRejected
            }
          }

          "a patch containing a 'test' value that is not a string (and so cannot be a unit type acronym)" in new PayeFixture {
            val invalidValuePatch = Seq(
              TestOperation(s"/children/$PayeRef", JsNumber(42)),
              RemoveOperation(s"/children/$PayeRef")
            )

            whenReady(patchService.applyPatchTo(LegalUnitKey, invalidValuePatch)) { result =>
              result shouldBe PatchRejected
            }
          }
        }
      }

      "signalling when a patch cannot be applied as a result of a conflicting change" in new DeleteVatFixture {
        (repository.deleteChildLink _).expects(LegalUnitKey, ValueAddedTax, VatUnitId).returning(
          Future.successful(EditConflicted)
        )

        whenReady(patchService.applyPatchTo(LegalUnitKey, DeleteVatChildPatch)) { result =>
          result shouldBe PatchConflicted
        }
      }

      "signalling when the target Legal Unit cannot be found" in new DeletePayeFixture {
        (repository.deleteChildLink _).expects(LegalUnitKey, PayAsYouEarn, PayeUnitId).returning(
          Future.successful(EditTargetNotFound)
        )

        whenReady(patchService.applyPatchTo(LegalUnitKey, DeletePayeChildPatch)) { result =>
          result shouldBe PatchTargetNotFound
        }
      }

      "signalling when the application of the patch fails" in new DeleteVatFixture {
        (repository.deleteChildLink _).expects(LegalUnitKey, ValueAddedTax, VatUnitId).returning(
          Future.successful(EditFailed)
        )

        whenReady(patchService.applyPatchTo(LegalUnitKey, DeleteVatChildPatch)) { result =>
          result shouldBe PatchFailed
        }
      }

      "rejecting" - {
        /*
         * Permitting an outright 'remove' operation would not allow us to use a 'checkAndDelete' operation,
         * and would run the risk of overwriting another user's concurrent changes.
         */
        "a patch that does not pair a 'test' operation with the 'remove' operation" in new Fixture {
          whenReady(patchService.applyPatchTo(LegalUnitKey, Seq(RemoveOperation(s"/children/$OtherRef")))) { result =>
            result shouldBe PatchRejected
          }
        }

        "a patch that attempts to delete a path other than '/children/<some-id>'" in new VatFixture {
          val invalidPatch = Seq(
            TestOperation(s"/child/$VatRef", JsString(toAcronym(ValueAddedTax))),
            RemoveOperation(s"/child/$VatRef")
          )

          whenReady(patchService.applyPatchTo(LegalUnitKey, invalidPatch)) { result =>
            result shouldBe PatchRejected
          }
        }

        "a patch containing a 'test' value that is not the acronym for either the VAT or PAYE unit type" in new Fixture {
          val invalidValuePatch = Seq(
            TestOperation(s"/children/$OtherRef", JsString(toAcronym(CompaniesHouse))),
            RemoveOperation(s"/children/$OtherRef")
          )

          whenReady(patchService.applyPatchTo(LegalUnitKey, invalidValuePatch)) { result =>
            result shouldBe PatchRejected
          }
        }
      }
    }

    "rejects a patch that does not represent either an add operation or a delete operation" in new VatFixture {
      val updatePatch = Seq(
        TestOperation(s"/children/$VatRef", JsString(toAcronym(ValueAddedTax))),
        ReplaceOperation(s"/children/$VatRef", JsString(toAcronym(PayAsYouEarn)))
      )

      whenReady(patchService.applyPatchTo(LegalUnitKey, updatePatch)) { result =>
        result shouldBe PatchRejected
      }
    }
  }
}
