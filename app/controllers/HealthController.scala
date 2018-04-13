package controllers

import io.swagger.annotations.{ Api, ApiOperation, ApiResponse, ApiResponses }
import org.joda.time.DateTime

import play.api.mvc.{ Controller, Action }

@Api("Utils")
class HealthController extends Controller {
  private[this] val startTime = System.currentTimeMillis()

  //public api
  @ApiOperation(
    value = "Application Health",
    notes = "Provides a json object containing minimal information on application live status and uptime.",
    httpMethod = "GET"
  )
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Success - Displays a json object of basic api health.")
  ))
  def health = Action {
    val uptimeInMillis = uptime()
    Ok(s"{Status: Ok, Uptime: ${uptimeInMillis}ms, Date and Time: " + new DateTime(startTime) + "}").as(JSON)
  }

  private def uptime(): Long = {
    val uptimeInMillis = System.currentTimeMillis() - startTime
    uptimeInMillis
  }

}
