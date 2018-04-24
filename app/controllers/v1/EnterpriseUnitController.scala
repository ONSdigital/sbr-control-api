package controllers.v1

import javax.inject.{ Inject, Singleton }

import scala.concurrent.ExecutionContext.Implicits.global

import play.api.mvc.{ Action, AnyContent, Controller }
import io.swagger.annotations._

import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.{ Enterprise, Ern }

import controllers.v1.ControllerUtils._
import repository.EnterpriseUnitRepository

@Api("Search")
@Singleton
class EnterpriseUnitController @Inject() (repository: EnterpriseUnitRepository) extends Controller {

  @ApiOperation(
    value = "Json representation of the Enterprise Unit with specified ERN and Period",
    notes = "Requires an exact match of ERN and Period",
    response = classOf[Enterprise],
    responseContainer = "object",
    code = 200,
    httpMethod = "GET"
  )
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "One or more argument does not comply with the expected format"),
    new ApiResponse(code = 404, message = "A Enterprise Unit could not be found with the specified ERN and Period"),
    new ApiResponse(code = 500, message = "The attempt to retrieve a Local Unit could not complete due to some failure"),
    new ApiResponse(code = 504, message = "A response was not received from the database within the required time interval")
  ))
  def retrieveEnterpriseUnit(
    @ApiParam(value = "Enterprise Reference Number (ERN)", example = "1000000012", required = true) ernStr: String,
    @ApiParam(value = "Period (unit load date)", example = "201803", required = true) periodStr: String
  ): Action[AnyContent] = Action.async {
    repository.retrieveEnterpriseUnit(Ern(ernStr), Period.fromString(periodStr)).map { errorOrOptEnterprise =>
      errorOrOptEnterprise.fold(resultOnFailure, resultOnSuccess[Enterprise])
    }
  }

}
