package handlers.http

import handlers.PatchHandler
import javax.inject.Inject
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.Result
import play.api.mvc.Results.{ Conflict, InternalServerError, NoContent, NotFound, UnprocessableEntity }
import services._
import uk.gov.ons.sbr.models.UnitKey
import uk.gov.ons.sbr.models.patch.Patch

import scala.concurrent.Future

class HttpPatchHandler @Inject() (patchService: PatchService) extends PatchHandler[Future[Result]] {
  override def apply(unitKey: UnitKey, patch: Patch): Future[Result] =
    patchService.applyPatchTo(unitKey, patch).map {
      case PatchApplied => NoContent
      case PatchConflicted => Conflict
      case PatchTargetNotFound => NotFound
      case PatchRejected => UnprocessableEntity
      case PatchFailed => InternalServerError
    }
}
