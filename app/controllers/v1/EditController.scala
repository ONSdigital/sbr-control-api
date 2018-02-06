package controllers.v1

import javax.inject.Inject

import scala.collection.JavaConversions._
import scala.util.{ Failure, Success, Try }

import play.api.mvc.{ Action, AnyContent }
import play.api.{ Configuration, Logger }
import io.swagger.annotations._

import utils.FutureResponse.futureSuccess
import utils.Utilities.errAsJson
import utils._

/**
 * Created by coolit on 20/09/2017.
 */
@Api("Edit")
class EditController @Inject() (val configuration: Configuration) extends ControllerUtils {

  @ApiOperation(
    value = "Ok if edit is made",
    notes = "Invokes a method in sbr-hbase-connector to edit an Enterprise",
    responseContainer = "JSONObject",
    code = 200,
    httpMethod = "POST"
  )
  @ApiResponses(Array(
    new ApiResponse(code = 200, responseContainer = "JsValue", message = "Edit has been made successfully to Enterprise with id: [id]"),
    new ApiResponse(code = 400, responseContainer = "JsValue", message = "BadRequest -> id or edit json is invalid"),
    new ApiResponse(code = 500, responseContainer = "JsValue", message = "InternalServerError -> Unable to make edit")
  ))
  def editEnterprise(
    @ApiParam(value = "An Enterprise ID", example = "1234567890", required = true) id: String
  ): Action[AnyContent] = Action.async { implicit request =>
    Logger.info(s"Editing by default period for id: $id")
    val evalResp = matchByEditParams(Some(id), request)
    val res = evalResp match {
      case (x: EditRequest) => {
        val resp = Try(requestEnterprise.updateEnterpriseVariableValues(x.id, x.updatedBy, x.edits)) match {
          case Success(s) => Ok(errAsJson(OK, "edit_success", s"Edit has been made successfully to Enterprise with id: ${x.id}")).future
          case Failure(ex) => InternalServerError(errAsJson(INTERNAL_SERVER_ERROR, "edit_error", s"unable to make edit with exception [${ex.printStackTrace()}]")).future
        }
        resp
      }
      case (i: InvalidKey) => BadRequest(errAsJson(BAD_REQUEST, "invalid_key", s"invalid id ${i.id}. Check key size[$minKeyLength].")).future
      case (j: InvalidEditJson) => BadRequest(errAsJson(BAD_REQUEST, "invalid_edit_json",
        s"cannot parse json with exception ${j.exception}")).future
    }
    res
  }

  @ApiOperation(
    value = "Ok if edit is made",
    notes = "Invokes a method in sbr-hbase-connector to edit an Enterprise",
    responseContainer = "JSONObject",
    code = 200,
    httpMethod = "POST"
  )
  @ApiResponses(Array(
    new ApiResponse(code = 200, responseContainer = "JsValue", message = "Edit has been made successfully to Enterprise with id: [id]"),
    new ApiResponse(code = 400, responseContainer = "JsValue", message = "BadRequest -> id or edit json or period is invalid"),
    new ApiResponse(code = 500, responseContainer = "JsValue", message = "InternalServerError -> Unable to make edit")
  ))
  def editEnterpriseForPeriod(
    @ApiParam(value = "A period in yyyyMM format", example = "201706", required = true) period: String,
    @ApiParam(value = "An Enterprise ID", example = "1234567890", required = true) id: String
  ): Action[AnyContent] = Action.async { implicit request =>
    Logger.info(s"Editing by period [$period] for id: $id")
    val evalResp = matchByEditParams(Some(id), request, Some(period))
    val res = evalResp match {
      case (x: EditRequestByPeriod) => {
        val resp = Try(requestEnterprise.updateEnterpriseVariableValues(x.period, x.id, x.updatedBy, x.edits)) match {
          case Success(s) => Ok(errAsJson(OK, "edit_success", s"Edit has been made successfully to Enterprise with id: ${x.id}, for period: ${x.period.toString}")).future
          case Failure(ex) => InternalServerError(errAsJson(INTERNAL_SERVER_ERROR, "edit_error", s"unable to make edit with exception [${ex.printStackTrace()}]")).future
        }
        resp
      }
      case (i: InvalidKey) => BadRequest(errAsJson(BAD_REQUEST, "invalid_key", s"invalid id ${i.id}. Check key size[$minKeyLength].")).future
      case (r: InvalidReferencePeriod) => BadRequest(errAsJson(BAD_REQUEST, "invalid_date", s"cannot parse date with exception ${r.exception}")).future
      case (j: InvalidEditJson) => BadRequest(errAsJson(BAD_REQUEST, "invalid_edit_json", s"cannot parse json with exception ${j.exception}")).future
    }
    res
  }
}
