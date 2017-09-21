package controllers

import javax.inject.Singleton
import io.swagger.annotations.{ Api, ApiOperation, ApiResponse, ApiResponses }

import play.api.mvc.{ Action, Controller }

/**
 * version listings is defined using the BuildInfo feature
 */
@Api("Utils")
@Singleton
class VersionController extends Controller {

  // public api
  @ApiOperation(
    value = "Version List",
    notes = "Provides a full listing of all versions of software related tools - this can be found in the build file.",
    httpMethod = "GET"
  )
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Success - Displays a version list as json.")
  ))
  def version = Action {
    Ok(BuildInfo.toJson).as(JSON)
  }
}