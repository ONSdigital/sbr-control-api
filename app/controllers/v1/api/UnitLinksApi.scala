package controllers.v1.api

import play.api.mvc.{ Action, AnyContent }
import io.swagger.annotations._
import uk.gov.ons.sbr.models.patch.Patch
import uk.gov.ons.sbr.models.unitlinks.UnitLinks

trait UnitLinksApi {
  // FIXME: this does not generate a useful "example" model of the resulting JSON
  @ApiOperation(
    value = "Json representation of the Unit Links with specified Unit Id, Unit Type and Period",
    notes = "Requires an exact match of Unit Id, Unit Type and Period",
    response = classOf[UnitLinks],
    responseContainer = "object",
    code = 200,
    httpMethod = "GET"
  )
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "One or more argument do not comply with the expected format"),
    new ApiResponse(code = 404, message = "An Unit Links could not be found with the specified Unit Id, Unit Type and Period"),
    new ApiResponse(code = 500, message = "The attempt to retrieve a Unit Links failed due to an internal server error"),
    new ApiResponse(code = 504, message = "A response was not received from the database within the required time interval")
  ))
  def retrieveUnitLinks(
    @ApiParam(value = "Unit Identifier", example = "1000000012", required = true) unitIdStr: String,
    @ApiParam(value = "Period (unit load date)", example = "201803", required = true) periodStr: String,
    @ApiParam(value = "Unit type", example = "ENT", required = true) unitTypeStr: String
  ): Action[AnyContent]

  // FIXME: this does not generate a suitable example of a json patch document
  // FIXME: this does not specify the required content type - Swagger UI assumes application/json which we will reject
  @ApiOperation(
    value = "Supports restricted editing of the links between units",
    code = 204,
    httpMethod = "PATCH"
  )
  @ApiImplicitParams(Array(
    new ApiImplicitParam(
      value = "a JSON Patch specification (see RFC6902)",
      paramType = "body",
      dataType = "controllers.v1.api.examples.unitlinks.TestOperationForSwagger",
      required = true
    )
  ))
  @ApiResponses(Array(
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
  ): Action[Patch]
}