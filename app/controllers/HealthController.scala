package controllers

import java.time._
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME

import io.swagger.annotations.{Api, ApiOperation, ApiResponse, ApiResponses}
import javax.inject.{Inject, Singleton}
import play.api.mvc.ControllerComponents

@Api("Utils")
@Singleton
class HealthController @Inject() (controllerComponents: ControllerComponents) extends AbstractSbrController(controllerComponents) {
  private[this] val startedAt: Instant = Instant.now()

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
    Ok(s"{Status: Ok, Uptime: ${uptime.toMillis}ms, Date and Time: ${startLocalDateTime.format(ISO_LOCAL_DATE_TIME)}").as(JSON)
  }

  private def uptime: Duration =
    Duration.between(startedAt, Instant.now())

  private def startLocalDateTime: LocalDateTime =
    LocalDateTime.ofInstant(startedAt, ZoneId.of("Europe/London"))
}
