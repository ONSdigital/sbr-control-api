package controllers.v1.api

import io.swagger.annotations.{ ApiOperation, ApiParam, ApiResponse, ApiResponses }
import play.api.mvc.{ Action, AnyContent }
import uk.gov.ons.sbr.models.reportingunit.ReportingUnit

trait ReportingUnitApi {
  @ApiOperation(
    value = "Json representation of the Reporting Unit with specified ERN, RURN and Period",
    notes = "Requires an exact match of ERN, RURN and Period",
    response = classOf[ReportingUnit],
    code = 200,
    httpMethod = "GET"
  )
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "One or more arguments does not comply with the expected format"),
    new ApiResponse(code = 404, message = "A Reporting Unit could not be found with the specified ERN, RURN and Period"),
    new ApiResponse(code = 500, message = "The attempt to retrieve a Reporting Unit could not complete due to some failure"),
    new ApiResponse(code = 504, message = "A response was not received from the database within the required time interval")
  ))
  def retrieveReportingUnit(
    @ApiParam(value = "Enterprise Reference Number (ERN) - a ten digit number", example = "1000000012", required = true) ernStr: String,
    @ApiParam(value = "Period (unit load date) - in YYYYMM format", example = "201803", required = true) periodStr: String,
    @ApiParam(value = "Reporting Unit Reference Number (RURN) - an eleven digit number", example = "33000000000", required = true) rurnStr: String
  ): Action[AnyContent]

  /*
   * Note that it is unusual for a RESTful service to return 404 for a "collection" resource; the typical response
   * ia a 200 with "empty collection" representation.
   * A 404 is justified here by the expectation in our data model that any existing enterprise / period combination
   * should have at least one reporting unit.  The 404 therefore indicates that there is no such collection resource for
   * an unknown enterprise / period - which is semantically different from a resource existing that is empty.
   */
  @ApiOperation(
    value = "A Json array representing all Reporting Units with the specified ERN and Period",
    response = classOf[ReportingUnit],
    responseContainer = "List", // result is an unadorned JSON Array
    code = 200,
    httpMethod = "GET"
  )
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "One or more arguments does not comply with the expected format"),
    new ApiResponse(code = 404, message = "A Reporting Unit could not be found for the specified ERN and Period"),
    new ApiResponse(code = 500, message = "The attempt to find Reporting Units could not complete due to some failure"),
    new ApiResponse(code = 504, message = "A response was not received from the database within the required time interval")
  ))
  def retrieveAllReportingUnitsForEnterprise(
    @ApiParam(value = "Enterprise Reference Number (ERN) - a ten digit number", example = "1000000012", required = true) ernStr: String,
    @ApiParam(value = "Period (unit load date) - in YYYYMM format", example = "201803", required = true) periodStr: String
  ): Action[AnyContent]
}
