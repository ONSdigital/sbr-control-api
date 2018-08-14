package controllers.v1

import com.typesafe.config.Config
import controllers.BuildInfo
import io.swagger.annotations.{ Api, ApiOperation, ApiResponse, ApiResponses }
import javax.inject.{ Inject, Singleton }
import play.api.libs.json.Json
import play.api.mvc.{ Action, AnyContent, Controller }

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
  def latestListings: Action[AnyContent] = Action {
    Ok(Json.obj("status" -> "OK", "bi-api-deployed-date" -> s"${BuildInfo.builtAtMillis}"))
  }

}
