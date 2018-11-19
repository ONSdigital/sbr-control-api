package controllers.v1

import controllers.AbstractSbrController
import controllers.v1.ControllerResultProcessor._
import controllers.v1.api.UnitLinksApi
import handlers.PatchHandler
import io.swagger.annotations._
import javax.inject.{Inject, Singleton}
import parsers.JsonPatchBodyParser
import play.api.mvc._
import repository.UnitLinksRepository
import uk.gov.ons.sbr.models.patch.Patch
import uk.gov.ons.sbr.models.unitlinks.UnitType.{LegalUnit, PayAsYouEarn, ValueAddedTax}
import uk.gov.ons.sbr.models.unitlinks.{UnitId, UnitLinks, UnitType}
import uk.gov.ons.sbr.models.{Period, UnitKey}

import scala.concurrent.Future

/*
 * Note that the Swagger annotations for edit have been moved from the trait, as this seems to cause issues for the
 * generator.  However, the example below still does not generate a useful example of the expected request payload.
 * This seems to be a known problem with the version of the annotations we are using.
 * Note also that annotation values must be constants, and so any attempt to format values such as 'notes' nicely
 * results in exceptions at runtime.
 */
@Api("Search")
@Singleton
class UnitLinksController @Inject() (controllerComponents: ControllerComponents,
                                     repository: UnitLinksRepository,
                                     handlePatch: PatchHandler[Future[Result]]) extends AbstractSbrController(controllerComponents) with UnitLinksApi {
  override def retrieveUnitLinks(id: String, periodStr: String, unitTypeStr: String): Action[AnyContent] = Action.async {
    val unitKey = unitKeyFor(UnitType.fromAcronym(unitTypeStr), id, periodStr)
    repository.retrieveUnitLinks(unitKey).map { errorOrOptUnitLinks =>
      errorOrOptUnitLinks.fold(resultOnFailure, resultOnSuccessWithAtMostOneUnit[UnitLinks])
    }
  }

  @ApiOperation(
    value = "Supports restricted editing of the links from a VAT unit",
    notes = "Use the following template:\n  [{\"op\": \"test\", \"path\": \"/parents/LEU\", \"value\": \"1234567890111111\"},\n {\"op\": \"replace\", \"path\": \"/parents/LEU\", \"value\": \"1234567890999999\"}]",
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
    new ApiResponse(code = 500, message = "The attempt to apply the specified patch encountered an unrecoverable failure")
  ))
  def patchVatUnitLinks(
    @ApiParam(value = "VAT reference", example = "123456789012", required = true) vatref: String,
    @ApiParam(value = "Period (unit load date)", example = "201803", required = true) periodStr: String
  ): Action[Patch] = Action.async(JsonPatchBodyParser) { request =>
    processUnitLinkPatch(ValueAddedTax, vatref, periodStr, request.body)
  }

  @ApiOperation(
    value = "Supports restricted editing of the links from a PAYE unit",
    notes = "Use the following template:\n  [{\"op\": \"test\", \"path\": \"/parents/LEU\", \"value\": \"1234567890111111\"},\n {\"op\": \"replace\", \"path\": \"/parents/LEU\", \"value\": \"1234567890999999\"}]",
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
    new ApiResponse(code = 400, message = "Either the request body does not comply with the Json Patch specification; or the period or PAYE reference arguments are invalid"),
    new ApiResponse(code = 404, message = "A PAYE unit could not be found with the target reference for the target period"),
    new ApiResponse(code = 409, message = "The requested update conflicts with that of another user"),
    new ApiResponse(code = 415, message = "The request content type is not that of Json Patch"),
    new ApiResponse(code = 422, message = "While the request body defines a valid Json Patch, it is not suitable for a PAYE unit"),
    new ApiResponse(code = 500, message = "The attempt to apply the specified patch encountered an unrecoverable failure")
  ))
  def patchPayeUnitLinks(
    @ApiParam(value = "PAYE reference", example = "575H7Z71278", required = true) payeref: String,
    @ApiParam(value = "Period (unit load date)", example = "201803", required = true) periodStr: String
  ): Action[Patch] =
    Action.async(JsonPatchBodyParser) { request =>
      processUnitLinkPatch(PayAsYouEarn, payeref, periodStr, request.body)
    }

  @ApiOperation(
    value = "Supports restricted editing of the links from a Legal Unit",
    notes = "Use the following template to create a child:\n  [{\"op\": \"add\", \"path\": \"/children/123456789012\", \"value\": \"VAT\"}]\n\nUse the following template to delete a child:\n  [{\"op\": \"test\", \"path\": \"/children/123456789012\", \"value\": \"VAT\"},\n {\"op\": \"remove\", \"path\": \"/children/123456789012\"}]",
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
        value = """[{"op": "add", "path": "/children/123456789012", "value": "VAT"}]"""
      )
    )),
      required = true
    )
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 204, message = "The patch was successfully applied"),
    new ApiResponse(code = 400, message = "Either the request body does not comply with the Json Patch specification; or the period or UBRN arguments are invalid"),
    new ApiResponse(code = 404, message = "A Legal Unit could not be found with the target UBRN for the target period"),
    new ApiResponse(code = 415, message = "The request content type is not that of Json Patch"),
    new ApiResponse(code = 422, message = "While the request body defines a valid Json Patch, it is not suitable for a Legal Unit"),
    new ApiResponse(code = 500, message = "The attempt to apply the specified patch encountered an unrecoverable failure")
  ))
  def patchLeuUnitLinks(
    @ApiParam(value = "The Legal Unit identifier", example = "1234567890123456", required = true) ubrn: String,
    @ApiParam(value = "Period (unit load date)", example = "201803", required = true) periodStr: String
  ): Action[Patch] = Action.async(JsonPatchBodyParser) { request =>
    processUnitLinkPatch(LegalUnit, ubrn, periodStr, request.body)
  }

  private def processUnitLinkPatch(unitType: UnitType, id: String, periodStr: String, patch: Patch): Future[Result] =
    handlePatch(unitKeyFor(unitType, id, periodStr), patch)

  private def unitKeyFor(unitType: UnitType, id: String, periodStr: String): UnitKey =
    UnitKey(UnitId(id), unitType, Period.fromString(periodStr))
}
