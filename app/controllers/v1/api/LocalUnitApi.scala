package controllers.v1.api

import uk.gov.ons.sbr.models.localunit.LocalUnit
import play.api.mvc.{Action, AnyContent}
import io.swagger.annotations.{ApiOperation, ApiParam, ApiResponse, ApiResponses}

trait LocalUnitApi {
  @ApiOperation(
    value = "Json representation of the Local Unit with specified ERN, LURN and Period",
    notes = "Requires an exact match of ERN, LURN and Period",
    response = classOf[LocalUnit],
    code = 200,
    httpMethod = "GET"
  )
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "One or more arguments does not comply with the expected format"),
    new ApiResponse(code = 404, message = "A Local Unit could not be found with the specified ERN, LURN and Period"),
    new ApiResponse(code = 500, message = "The attempt to retrieve a Local Unit could not complete due to some failure"),
    new ApiResponse(code = 504, message = "A response was not received from the database within the required time interval")
  ))
  def retrieveLocalUnit(
    @ApiParam(value = "Enterprise Reference Number (ERN) - a ten digit number", example = "1000000012", required = true) ernStr: String,
    @ApiParam(value = "Period (unit load date) - in YYYYMM format", example = "201803", required = true) periodStr: String,
    @ApiParam(value = "Local Unit Reference Number (LURN) - a nine digit number", example = "900000011", required = true) lurnStr: String
  ): Action[AnyContent]

  /*
   * Note that it is unusual for a RESTful service to return 404 for a "collection" resource; the typical response
   * ia a 200 with "empty collection" representation.
   * A 404 is justified here by the expectation in our data model that any existing enterprise / period combination
   * should have at least one local unit.  The 404 therefore indicates that there is no such collection resource for
   * an unknown enterprise / period - which is semantically different from a resource existing that is empty.
   */
  @ApiOperation(
    value = "A Json array representing all Local Units with the specified ERN and Period",
    response = classOf[LocalUnit],
    responseContainer = "List", // result is an unadorned JSON Array
    code = 200,
    httpMethod = "GET"
  )
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "One or more arguments does not comply with the expected format"),
    new ApiResponse(code = 404, message = "A Local Unit could not be found for the specified ERN and Period"),
    new ApiResponse(code = 500, message = "The attempt to find Local Units could not complete due to some failure"),
    new ApiResponse(code = 504, message = "A response was not received from the database within the required time interval")
  ))
  def retrieveAllLocalUnitsForEnterprise(
    @ApiParam(value = "Enterprise Reference Number (ERN) - a ten digit number", example = "1000000012", required = true) ernStr: String,
    @ApiParam(value = "Period (unit load date) - in YYYYMM format", example = "201803", required = true) periodStr: String
  ): Action[AnyContent]
}
