package services

import java.time.Month.SEPTEMBER

import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ FreeSpec, Matchers }
import uk.gov.ons.sbr.models.unitlinks.UnitId
import uk.gov.ons.sbr.models.unitlinks.UnitType.{ CompaniesHouse, Enterprise, LegalUnit, LocalUnit, PayAsYouEarn, ReportingUnit, ValueAddedTax }
import uk.gov.ons.sbr.models.{ Period, UnitKey }

import scala.concurrent.Future

class UnitLinksPatchServiceSpec extends FreeSpec with Matchers with MockFactory with ScalaFutures {

  private trait Fixture {
    val DummyPatch = Seq.empty
    val RegisterPeriod = Period.fromYearMonth(2018, SEPTEMBER)
    val patchStatuses = List(PatchApplied, PatchConflicted, PatchTargetNotFound, PatchRejected, PatchFailed)
    val unsupportedUnitTypes = Set(CompaniesHouse, Enterprise, LocalUnit, ReportingUnit)

    val adminDataPatchService = mock[PatchService]
    val legalUnitPatchService = mock[PatchService]
    val unitLinksPatchService = new UnitLinksPatchService(adminDataPatchService, legalUnitPatchService)
  }

  private trait VatFixture extends Fixture {
    val VatUnitKey = UnitKey(UnitId("123456789012"), ValueAddedTax, RegisterPeriod)
  }

  private trait PayeFixture extends Fixture {
    val PayeUnitKey = UnitKey(UnitId("1234ABCD"), PayAsYouEarn, RegisterPeriod)
  }

  private trait LegalUnitFixture extends Fixture {
    val LegalUnitKey = UnitKey(UnitId("1234567890123456"), LegalUnit, RegisterPeriod)
  }

  "A UnitLinks PatchService" - {
    "uses the admin data patch service to apply patches to the links from a VAT unit" in new VatFixture {
      patchStatuses.foreach { status =>
        withClue(s"when vatPatchService returns $status") {
          (adminDataPatchService.applyPatchTo _).expects(VatUnitKey, DummyPatch).returning(Future.successful(status))

          whenReady(unitLinksPatchService.applyPatchTo(VatUnitKey, DummyPatch)) { result =>
            result shouldBe status
          }
        }
      }
    }

    "uses the admin data patch service to apply patches to the links from a PAYE unit" in new PayeFixture {
      patchStatuses.foreach { status =>
        withClue(s"when payePatchService returns $status") {
          (adminDataPatchService.applyPatchTo _).expects(PayeUnitKey, DummyPatch).returning(Future.successful(status))

          whenReady(unitLinksPatchService.applyPatchTo(PayeUnitKey, DummyPatch)) { result =>
            result shouldBe status
          }
        }
      }
    }

    "uses the Legal Unit patch service to apply patches to the links from a Legal Unit" in new LegalUnitFixture {
      patchStatuses.foreach { status =>
        withClue(s"when legalUnitPatchService returns $status") {
          (legalUnitPatchService.applyPatchTo _).expects(LegalUnitKey, DummyPatch).returning(Future.successful(status))

          whenReady(unitLinksPatchService.applyPatchTo(LegalUnitKey, DummyPatch)) { result =>
            result shouldBe status
          }
        }
      }
    }

    "rejects patches to unit types other than VAT, PAYE and Legal Units" in new Fixture {
      unsupportedUnitTypes.foreach { unitType =>
        withClue(s"when the unsupported unitType is $unitType") {
          whenReady(unitLinksPatchService.applyPatchTo(UnitKey(UnitId("1234"), unitType, RegisterPeriod), DummyPatch)) { result =>
            result shouldBe PatchRejected
          }
        }
      }
    }
  }
}
