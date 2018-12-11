package services

import javax.inject.Inject
import repository._
import services.AdminUnitLinkPatchService.{ParentLegalUnit, toPatchStatus}
import uk.gov.ons.sbr.models.UnitKey
import uk.gov.ons.sbr.models.patch.{Patch, ReplaceOperation, TestOperation}
import uk.gov.ons.sbr.models.unitlinks.UnitType.LegalUnit
import uk.gov.ons.sbr.models.unitlinks.{UnitId, UnitType}
import utils.JsResultSupport

import scala.concurrent.{ExecutionContext, Future}

class AdminUnitLinkPatchService @Inject() (repository: UnitLinksRepository,
                                           unitRegisterService: UnitRegisterService)
                                          (implicit ec: ExecutionContext) extends PatchService {
  override def applyPatchTo(unitKey: UnitKey, patch: Patch): Future[PatchStatus] =
    patch match {
      case TestOperation(ParentLegalUnit, fromValue) :: ReplaceOperation(ParentLegalUnit, toValue) :: Nil =>
        JsResultSupport.map2(fromValue.validate[UnitId], toValue.validate[UnitId])(_ -> _).fold(
          invalid = _ => Future.successful(PatchRejected),
          valid = {
          case (fromUbrn, toUbrn) =>
            updateParentUbrn(unitKey, fromUbrn, toUbrn)
        }
        )
      case _ =>
        Future.successful(PatchRejected)
    }

  /*
   * We check the target Legal Unit exists before updating a link to point to it.
   * This is a "best effort" attempt to maintain the consistency of the database.  As we have no way to perform an
   * atomic operation across tables, there is a risk (in future) that a concurrent process could delete the target
   * Legal Unit between the check and the update steps.  For now, we assume that units will not be outright deleted
   * and that this is sufficient.
   */
  private def updateParentUbrn(unitKey: UnitKey, fromUbrn: UnitId, toUbrn: UnitId): Future[PatchStatus] =
    unitRegisterService.isRegisteredUnit(UnitKey(toUbrn, LegalUnit, unitKey.period)).flatMap {
      case UnitFound =>
        repository.updateParentLink(unitKey, UpdateParentDescriptor(LegalUnit, fromUbrn, toUbrn)).map(toPatchStatus)
      case UnitNotFound =>
        Future.successful(PatchRejected)
      case UnitRegisterFailure(_) =>
        Future.successful(PatchFailed)
    }
}

private object AdminUnitLinkPatchService {
  val ParentLegalUnit = "/parents/" + UnitType.toAcronym(LegalUnit)

  def toPatchStatus(editResult: OptimisticEditResult): PatchStatus =
    editResult match {
      case EditApplied => PatchApplied
      case EditConflicted => PatchConflicted
      case EditTargetNotFound => PatchTargetNotFound
      case EditFailed => PatchFailed
    }
}