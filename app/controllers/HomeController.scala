package controllers

import io.swagger.annotations.{ Api, ApiOperation, ApiResponse, ApiResponses }

import play.api.mvc.{ Controller, Action }

@Api("Utils")
class HomeController extends Controller {

  //public api
  @ApiOperation(
    value = "Swagger Documentation",
    notes = "Documentation of API endpoints for Swagger",
    httpMethod = "GET"
  )
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Success - Displays swagger documentation.")
  ))
  def swagger = Action { request =>
    val host = request.host
    Redirect(url = s"http://$host/assets/lib/swagger-ui/index.html", queryString = Map("url" -> Seq(s"http://$host/swagger.json")))
  }

  //public api
  @ApiOperation(
    value = "Application Health",
    notes = "Provides a json object containing minimal information on application live status and uptime.",
    httpMethod = "GET"
  )
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Success - Displays a json object of basic api health.")
  ))
  def status = Action { request =>
    val host = request.host
    Redirect(url = s"http://$host/health").flashing("redirect" -> "You are being redirected to health status", "status" -> "ok")
  }

}
