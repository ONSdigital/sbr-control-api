package controllers.v1

import io.swagger.annotations._
import javax.inject.{ Inject, Singleton }
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json.toJson
import play.api.mvc.{ Action, Controller, Result }
import repository.LocalUnitRepository
import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.localunit.{ LocalUnit, Lurn }

/*
 * Note that we are relying on regex patterns in the routes definitions to apply argument validation.
 * Only requests with valid arguments should be routed to the retrieveLocalUnit action.
 * All other requests should be routed to the badRequest action.
 */
@Api("Search")
@Singleton
class LocalUnitController @Inject() (repository: LocalUnitRepository) extends Controller {
  @ApiOperation(
    value = "Json representation of the Local Unit with specified ERN, LURN and Period",
    notes = "Requires an exact match of ERN, LURN and Period",
    response = classOf[LocalUnit],
    responseContainer = "object",
    code = 200,
    httpMethod = "GET"
  )
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "One or more argument does not comply with the expected format"),
    new ApiResponse(code = 404, message = "A Local Unit could not be found with the specified ERN, LURN and Period"),
    new ApiResponse(code = 500, message = "The attempt to retrieve a Local Unit could not complete due to some failure"),
    new ApiResponse(code = 504, message = "A response was not received from the database within the required time interval")
  ))
  def retrieveLocalUnit(
    @ApiParam(value = "Enterprise Reference Number (ERN) - a ten digit number", example = "1000000012", required = true) ernStr: String,
    @ApiParam(value = "Period (unit load date) - in YYYYMM format", example = "201803", required = true) periodStr: String,
    @ApiParam(value = "Local Unit Reference Number (LURN) - a nine digit number", example = "900000011", required = true) lurnStr: String
  ) = Action.async {
    repository.retrieveLocalUnit(Ern(ernStr), Period.fromString(periodStr), Lurn(lurnStr)).map { errorOrLocalUnit =>
      errorOrLocalUnit.fold(resultOnFailure, resultOnSuccess)
    }
  }

  private def resultOnFailure(errorMessage: String): Result =
    errorMessage match {
      case _ if errorMessage.startsWith("Timeout") => GatewayTimeout
      case _ => InternalServerError
    }

  private def resultOnSuccess(optLocalUnit: Option[LocalUnit]): Result =
    optLocalUnit.fold[Result](NotFound)(localUnit => Ok(toJson(localUnit)))

  def badRequest(ernStr: String, periodStr: String, lurnStr: String) = Action {
    BadRequest
  }
}
