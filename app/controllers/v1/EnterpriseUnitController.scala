package controllers.v1

import javax.inject.{ Inject, Singleton }

import play.api.libs.json.Json
import play.api.mvc.{ Action, AnyContent, Controller, Result }
import io.swagger.annotations._

import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.{ Enterprise, Ern }
import scala.concurrent.ExecutionContext.Implicits.global

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
    new ApiResponse(code = 404, message = "A Enterprise Unit could not be found with the specified ERN and Period")
  ))
  def retrieveEnterpriseUnit(
    @ApiParam(value = "Enterprise Reference Number (ERN)", example = "1000000012", required = true) ernStr: String,
    @ApiParam(value = "Period (unit load date)", example = "201803", required = true) periodStr: String
  ): Action[AnyContent] = Action.async {
    repository.retrieveEnterpriseUnit(Ern(ernStr), Period.fromString(periodStr)).map {
      optEnterprise =>
        optEnterprise.fold[Result](NotFound) { enterprise =>
          Ok(Json.toJson(enterprise))
        }
    }
  }

}
