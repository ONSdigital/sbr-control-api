package controllers.v1.api

import io.swagger.annotations.{ ApiOperation, ApiParam, ApiResponse, ApiResponses }
import play.api.mvc.{ Action, AnyContent }
import uk.gov.ons.sbr.models.legalunit.LegalUnit

trait LegalUnitApi {
  @ApiOperation(
    value = "Json representation of the Legal Unit with specified ERN, UBRN and Period",
    notes = "Requires an exact match of ERN, UBRN and Period",
    response = classOf[LegalUnit],
    code = 200,
    httpMethod = "GET"
  )
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "One or more arguments does not comply with the expected format"),
    new ApiResponse(code = 404, message = "A Legal Unit could not be found with the specified ERN, UBRN and Period"),
    new ApiResponse(code = 500, message = "The attempt to retrieve a Legal Unit could not complete due to some failure"),
    new ApiResponse(code = 504, message = "A response was not received from the database within the required time interval")
  ))
  def retrieveLegalUnit(
    @ApiParam(value = "Enterprise Reference Number (ERN) - a ten digit number", example = "1000000012", required = true) ernStr: String,
    @ApiParam(value = "Period (unit load date) - in YYYYMM format", example = "201803", required = true) periodStr: String,
    @ApiParam(value = "Legal Unit Reference Number (UBRN) - a sixteen digit number", example = "0000000000111111", required = true) ubrnStr: String
  ): Action[AnyContent]

  /*
   * Note that it is unusual for a RESTful service to return 404 for a "collection" resource; the typical response
   * ia a 200 with "empty collection" representation.
   * A 404 is justified here by the expectation in our data model that any existing enterprise / period combination
   * should have at least one legal unit.  The 404 therefore indicates that there is no such collection resource for
   * an unknown enterprise / period - which is semantically different from a resource existing that is empty.
   */
  @ApiOperation(
    value = "A Json array representing all Legal Units with the specified ERN and Period",
    response = classOf[LegalUnit],
    responseContainer = "List", // result is an unadorned JSON Array
    code = 200,
    httpMethod = "GET"
  )
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "One or more arguments does not comply with the expected format"),
    new ApiResponse(code = 404, message = "A Legal Unit could not be found for the specified ERN and Period"),
    new ApiResponse(code = 500, message = "The attempt to find Legal Units could not complete due to some failure"),
    new ApiResponse(code = 504, message = "A response was not received from the database within the required time interval")
  ))
  def retrieveAllLegalUnitsForEnterprise(
    @ApiParam(value = "Enterprise Reference Number (ERN) - a ten digit number", example = "1000000012", required = true) ernStr: String,
    @ApiParam(value = "Period (unit load date) - in YYYYMM format", example = "201803", required = true) periodStr: String
  ): Action[AnyContent]
}
