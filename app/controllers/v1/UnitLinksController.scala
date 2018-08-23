package controllers.v1

import controllers.v1.ControllerResultProcessor._
import controllers.v1.api.UnitLinksApi
import io.swagger.annotations.Api
import javax.inject.Inject
import parsers.JsonPatchBodyParser
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import repository.UnitLinksRepository
import services._
import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.patch.Patch
import uk.gov.ons.sbr.models.unitlinks.UnitType.ValueAddedTax
import uk.gov.ons.sbr.models.unitlinks.{ UnitId, UnitLinks, UnitType }

@Api("Search")
class UnitLinksController @Inject() (repository: UnitLinksRepository, patchService: PatchService) extends Controller with UnitLinksApi {
  def retrieveUnitLinks(id: String, periodStr: String, unitTypeStr: String): Action[AnyContent] = Action.async {
    repository.retrieveUnitLinks(UnitId(id), UnitType.fromAcronym(unitTypeStr), Period.fromString(periodStr)).map { errorOrOptUnitLinks =>
      errorOrOptUnitLinks.fold(resultOnFailure, resultOnSuccessWithAtMostOneUnit[UnitLinks])
    }
  }

  def patchVatUnitLinks(vatref: String, periodStr: String): Action[Patch] = Action.async(JsonPatchBodyParser) { request =>
    patchService.applyPatchTo(UnitId(vatref), ValueAddedTax, Period.fromString(periodStr), request.body).map {
      case PatchApplied => NoContent
      case PatchConflicted => Conflict
      case PatchTargetNotFound => NotFound
      case PatchRejected => UnprocessableEntity
      case PatchFailed => InternalServerError
    }
  }
}
