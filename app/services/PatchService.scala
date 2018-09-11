package services

import uk.gov.ons.sbr.models.UnitKey
import uk.gov.ons.sbr.models.patch.Patch

import scala.concurrent.Future

sealed trait PatchStatus
case object PatchApplied extends PatchStatus
case object PatchConflicted extends PatchStatus
case object PatchTargetNotFound extends PatchStatus
case object PatchRejected extends PatchStatus
case object PatchFailed extends PatchStatus

trait PatchService {
  def applyPatchTo(unitKey: UnitKey, patch: Patch): Future[PatchStatus]
}
