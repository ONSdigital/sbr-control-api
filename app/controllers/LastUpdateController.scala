package controllers.v1

import javax.inject.{ Inject, Singleton }

import com.typesafe.config.Config
import io.swagger.annotations.{ Api, ApiOperation, ApiResponse, ApiResponses }
import play.api.libs.json.Json
import play.api.mvc.{ Action, AnyContent, Controller }

import utils.FutureResponse.futureSuccess
import controllers.BuildInfo

@Api("Utils")
@Singleton
class LastUpdateController @Inject() (implicit val config: Config) extends Controller {

  @ApiOperation(
    value = "A Json list of dates representing dates of last changes made",
    notes = "Dates are typically for official releases (i.e. deployment not development level). Time is registered in system time millis.",
    httpMethod = "GET"
  )
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Success - Displays json list of dates for official development.")
  ))
  def latestListings: Action[AnyContent] = Action.async {
    Ok(Json.obj("status" -> "OK", "bi-api-deployed-date" -> s"${BuildInfo.builtAtMillis}")).future
  }

}
