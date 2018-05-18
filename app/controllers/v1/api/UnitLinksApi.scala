package controllers.v1.api

import play.api.mvc.{ Action, AnyContent }
import io.swagger.annotations.{ ApiOperation, ApiParam, ApiResponse, ApiResponses }

import uk.gov.ons.sbr.models.unitlinks.UnitLinks

trait UnitLinksApi {

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
  def retrieveUnitLinksWithPeriod(
    @ApiParam(value = "Unit Identifier (ERN)", example = "1000000012", required = true) unitIdStr: String,
    @ApiParam(value = "Period (unit load date)", example = "201803", required = true) periodStr: String,
    @ApiParam(value = "Underlying unit type", example = "ENT", required = true) unitTypeStr: String
  ): Action[AnyContent]

}