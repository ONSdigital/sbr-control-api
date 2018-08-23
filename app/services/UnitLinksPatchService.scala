package services

import javax.inject.Inject
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import repository._
import services.UnitLinksPatchService.{ ParentLegalUnit, asUnitId, toPatchStatus }
import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.patch.OperationTypes.{ Replace, Test }
import uk.gov.ons.sbr.models.patch.{ Operation, Patch }
import uk.gov.ons.sbr.models.unitlinks.UnitType.LegalUnit
import uk.gov.ons.sbr.models.unitlinks.{ UnitId, UnitType }
import utils.JsResultSupport

import scala.concurrent.Future

class UnitLinksPatchService @Inject() (repository: UnitLinksRepository, unitRegisterService: UnitRegisterService) extends PatchService {
  override def applyPatchTo(unitId: UnitId, unitType: UnitType, period: Period, patch: Patch): Future[PatchStatus] =
    patch match {
      case Operation(Test, ParentLegalUnit, fromValue) :: Operation(Replace, ParentLegalUnit, toValue) :: Nil =>
        JsResultSupport.map2(asUnitId(fromValue), asUnitId(toValue))(_ -> _).fold(
          invalid = _ => Future.successful(PatchRejected),
          valid = {
          case (fromUbrn, toUbrn) =>
            updateParentUbrn(unitId, unitType, period, fromUbrn, toUbrn)
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
  private def updateParentUbrn(unitId: UnitId, unitType: UnitType, period: Period, fromUbrn: UnitId, toUbrn: UnitId): Future[PatchStatus] =
    unitRegisterService.isRegisteredUnit(toUbrn, LegalUnit, period).flatMap {
      case UnitFound => repository.updateParentId(unitId, unitType, period, LegalUnit, fromUbrn, toUbrn).map(toPatchStatus)
      case UnitNotFound => Future.successful(PatchRejected)
      case UnitRegisterFailure(_) => Future.successful(PatchFailed)
    }
}

private object UnitLinksPatchService {
  val ParentLegalUnit = "/parents/" + UnitType.toAcronym(LegalUnit)

  def asString(jsValue: JsValue): JsResult[String] =
    jsValue.validate[JsString].map(_.value)

  def asUnitId(jsValue: JsValue): JsResult[UnitId] =
    asString(jsValue).map(UnitId.apply)

  def toPatchStatus(updateResult: UpdateResult): PatchStatus =
    updateResult match {
      case UpdateApplied => PatchApplied
      case UpdateConflicted => PatchConflicted
      case UpdateTargetNotFound => PatchTargetNotFound
      case UpdateFailed => PatchFailed
      case UpdateRejected => PatchFailed // there is nothing the client can do about this, so treat as a failure
    }
}