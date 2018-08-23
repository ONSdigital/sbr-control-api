package services

import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.patch.Patch
import uk.gov.ons.sbr.models.unitlinks.{ UnitId, UnitType }

import scala.concurrent.Future

sealed trait PatchStatus
case object PatchApplied extends PatchStatus
case object PatchConflicted extends PatchStatus
case object PatchTargetNotFound extends PatchStatus
case object PatchRejected extends PatchStatus
case object PatchFailed extends PatchStatus

trait PatchService {
  def applyPatchTo(unitId: UnitId, unitType: UnitType, period: Period, patch: Patch): Future[PatchStatus]
}
