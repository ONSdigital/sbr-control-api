package controllers.v1.api

import io.swagger.annotations._
import play.api.mvc.{ Action, AnyContent }
import uk.gov.ons.sbr.models.unitlinks.UnitLinks

/*
 * Note that the patch endpoint is not defined here, as it has become increasingly clear that the Swagger
 * documentation generator struggles when the annotations are defined on a trait as opposed to a concrete class.
 */
trait UnitLinksApi {
  // FIXME: this does not generate a useful "example" model of the JSON result
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
}