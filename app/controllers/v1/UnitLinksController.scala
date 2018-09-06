package controllers.v1

import controllers.v1.ControllerResultProcessor._
import controllers.v1.api.UnitLinksApi
import io.swagger.annotations._
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

/*
 * Note that the Swagger annotations for edit have been moved from the trait, as this seems to cause issues for the
 * generator.  However, the example below still does not generate a useful example of the expected request payload.
 * This seems to be a known problem with the version of the annotations we are using.
 */
@Api("Search")
class UnitLinksController @Inject() (repository: UnitLinksRepository, patchService: PatchService) extends Controller with UnitLinksApi {
  def retrieveUnitLinks(id: String, periodStr: String, unitTypeStr: String): Action[AnyContent] = Action.async {
    repository.retrieveUnitLinks(UnitId(id), UnitType.fromAcronym(unitTypeStr), Period.fromString(periodStr)).map { errorOrOptUnitLinks =>
      errorOrOptUnitLinks.fold(resultOnFailure, resultOnSuccessWithAtMostOneUnit[UnitLinks])
    }
  }

  @ApiOperation(
    value = "Supports restricted editing of the links between units",
    notes = """Use the following template: [{"op": "test", "path": "/parents/LEU", "value": "1234567890111111"},{"op": "replace", "path": "/parents/LEU", "value": "1234567890999999"}]""",
    consumes = "application/json-patch+json",
    code = 204,
    httpMethod = "PATCH"
  )
  @ApiImplicitParams(Array(
    new ApiImplicitParam(
      value = "a JSON Patch specification (see RFC6902)",
      paramType = "body",
      examples = new Example(Array(
      new ExampleProperty(
        mediaType = "application/json-patch+json",
        value = """[{"op": "test", "path": "/parents/LEU", "value": "1234567890111111"},{"op": "replace", "path": "/parents/LEU", "value": "1234567890999999"}]"""
      )
    )),
      required = true
    )
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 204, message = "The patch was successfully applied"),
    new ApiResponse(code = 400, message = "Either the request body does not comply with the Json Patch specification; or the period or VAT reference arguments are invalid"),
    new ApiResponse(code = 404, message = "A VAT unit could not be found with the target reference for the target period"),
    new ApiResponse(code = 409, message = "The requested update conflicts with that of another user"),
    new ApiResponse(code = 415, message = "The request content type is not that of Json Patch"),
    new ApiResponse(code = 422, message = "While the request body defines a valid Json Patch, it is not suitable for a VAT unit"),
    new ApiResponse(code = 500, message = "The attempt to update a unit link failed")
  ))
  def patchVatUnitLinks(
    @ApiParam(value = "VAT reference", example = "123456789012", required = true) vatref: String,
    @ApiParam(value = "Period (unit load date)", example = "201803", required = true) periodStr: String
  ): Action[Patch] = Action.async(JsonPatchBodyParser) { request =>
    patchService.applyPatchTo(UnitId(vatref), ValueAddedTax, Period.fromString(periodStr), request.body).map {
      case PatchApplied => NoContent
      case PatchConflicted => Conflict
      case PatchTargetNotFound => NotFound
      case PatchRejected => UnprocessableEntity
      case PatchFailed => InternalServerError
    }
  }
}
