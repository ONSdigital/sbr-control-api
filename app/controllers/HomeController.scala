package controllers

import io.swagger.annotations.{ Api, ApiOperation, ApiResponse, ApiResponses }
import play.api.mvc.{ Controller, _ }

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
    Redirect(url = s"http://${host}/assets/lib/swagger-ui/index.html", queryString = Map("url" -> Seq(s"http://${host}/swagger.json")))
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
    Redirect(url = s"http://${host}/health").flashing("redirect" -> "You are being redirected to health status", "status" -> "ok")
  }

  //public api
  @ApiOperation(
    value = "Permissions method request",
    notes = "pre-flight is used for local OPTIONS requests that precede PUT/DELETE requests. " +
    "An empty Ok() response allows the actual PUT/DELETE request to be sent.",
    httpMethod = "OPTIONS"
  )
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Success - Permission accepted with OK message"),
    new ApiResponse(code = 404, message = "Not Found - Root not Found"),
    new ApiResponse(code = 500, message = "Internal Server Error")
  )) // hack CORS
  def preflight(all: String) = Action {
    Ok("")
  }

}
