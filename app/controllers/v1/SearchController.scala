package controllers.v1

import play.api.mvc.{ Action, AnyContent }
import java.util.Optional

import io.swagger.annotations._
import uk.gov.ons.sbr.data.domain.{ Enterprise, StatisticalUnit }
import uk.gov.ons.sbr.models.Links
import uk.gov.ons.sbr.models.units.EnterpriseKey
import utils._

import scala.util.{ Failure, Success, Try }
import utils.Utilities.errAsJson
import utils.Properties.minKeyLength

import utils.FutureResponse._

/**
 * Created by haqa on 04/08/2017.
 */
@Api("Search")
class SearchController extends ControllerUtils {

  /**
   * @todo - generalise and create generic search function x2 per param length
   */
  //public api
  @ApiOperation(
    value = "Json response of links that correspond to id",
    notes = "Invokes a HBase api function to retrieve a nested link of potential parent and children by using the id param",
    responseContainer = "JSONObject",
    code = 200,
    httpMethod = "GET"
  )
  @ApiResponses(Array(
    new ApiResponse(code = 200, response = classOf[Links], responseContainer = "JsValue", message = "Ok -> Retrieved Links for given id."),
    new ApiResponse(code = 400, responseContainer = "JsValue", message = "BadRequest -> Id or other is invalid."),
    new ApiResponse(code = 404, responseContainer = "JsValue", message = "NotFound -> Given attributes could not be matched."),
    new ApiResponse(code = 500, responseContainer = "JsValue",
      message = "InternalServerError -> Failed to get valid response from endpoint this maybe due to connection timeout or invalid endpoint.")
  ))
  def retrieveLinksById(
    @ApiParam(value = "An identifier of any type", example = "825039145000", required = true) id: Option[String]
  ): Action[AnyContent] = Action.async { implicit request =>
    val res = matchByParams(id, request) match {
      case (x: IdRequest) =>
        val resp = Try(requestLinks.findUnits(x.id)) match {
          case Success(s) => if (s.isPresent) {
            resultMatcher[java.util.List[StatisticalUnit]](s, toScalaList)
          } else {
            NotFound(errAsJson(NOT_FOUND, "not_found", s"Could not find enterprise with id ${x.id}")).future
          }
          case Failure(ex) =>
            InternalServerError(errAsJson(INTERNAL_SERVER_ERROR, "internal_server_error", s"$ex")).future
        }
        resp
      case (i: InvalidKey) => BadRequest(errAsJson(BAD_REQUEST, "invalid_key", s"invalid id ${i.id}. Check key size[$minKeyLength].")).future
      case _ =>
        BadRequest(errAsJson(BAD_REQUEST, "missing_param", s"No query specified.")).future
    }
    res
  }

  //public api
  @ApiOperation(
    value = "Json response of links that correspond to id and date",
    notes = "Invokes a HBase api function to retrieve a nested link of potential parent and children by using the date and id param",
    responseContainer = "JSONObject",
    code = 200,
    httpMethod = "GET"
  )
  @ApiResponses(Array(
    new ApiResponse(code = 200, response = classOf[Links], responseContainer = "JsValue", message = "Ok -> Retrieved Links for given id."),
    new ApiResponse(code = 400, responseContainer = "JsValue", message = "BadRequest -> Id or other is invalid."),
    new ApiResponse(code = 404, responseContainer = "JsValue", message = "NotFound -> Given attributes could not be matched."),
    new ApiResponse(code = 500, responseContainer = "JsValue",
      message = "InternalServerError -> Failed to get valid response from endpoint this maybe due to connection timeout or invalid endpoint.")
  ))
  def retrieveLinks(
    @ApiParam(value = "Identifier creation date", example = "2017/07", required = true) date: String,
    @ApiParam(value = "An identifier of any type", example = "825039145000", required = true) id: Option[String]
  ): Action[AnyContent] = Action.async { implicit request =>
    println(s"res ===> ${matchByParams(id, request, Some(date))}")
    val res = matchByParams(id, request, Some(date)) match {
      case (x: ReferencePeriod) =>
        val resp = Try(requestLinks.findUnits(x.period, x.id)) match {
          case Success(s) => if (s.isPresent) {
            resultMatcher[java.util.List[StatisticalUnit]](s, toScalaList)
          } else {
            NotFound(errAsJson(NOT_FOUND, "not_found", s"Could not find enterprise with id ${x.id}")).future
          }
          case Failure(ex) =>
            InternalServerError(errAsJson(INTERNAL_SERVER_ERROR, "internal_server_error", s"$ex")).future
        }
        resp
      case (y: InvalidReferencePeriod) => BadRequest(errAsJson(BAD_REQUEST, "invalid_date",
        s"cannot parse date with exception ${y.exception}")).future
      case (i: InvalidKey) => BadRequest(errAsJson(BAD_REQUEST, "invalid_key", s"invalid id ${i.id}. Check key size[$minKeyLength].")).future
      case _ =>
        BadRequest(errAsJson(BAD_REQUEST, "missing_param", s"No query specified.")).future
    }
    res
  }

  //public api
  @ApiOperation(
    value = "Json response of matching id",
    notes = "Invokes a HBase api function to retrieve data by using the id param",
    responseContainer = "JSONObject",
    code = 200,
    httpMethod = "GET"
  )
  @ApiResponses(Array(
    new ApiResponse(code = 200, response = classOf[EnterpriseKey], responseContainer = "JsValue", message = "Ok -> Retrieved Enterprise for given id."),
    new ApiResponse(code = 400, responseContainer = "JsValue", message = "BadRequest -> Id or other is invalid."),
    new ApiResponse(code = 404, responseContainer = "JsValue", message = "NotFound -> Given attributes could not be matched."),
    new ApiResponse(code = 500, responseContainer = "JsValue",
      message = "InternalServerError -> Failed to get valid response from endpoint this maybe due to connection timeout or invalid endpoint.")
  ))
  def retrieveEnterpriseById(
    @ApiParam(value = "An identifier of any type", example = "1244", required = true) id: String
  ): Action[AnyContent] = Action.async { implicit request =>
    val res = matchByParams(Some(id), request) match {
      case (x: IdRequest) =>
        val resp = Try(requestEnterprise.getEnterprise(x.id)) match {
          case Success(s: Optional[Enterprise]) => if (s.isPresent) {
            resultMatcher[Enterprise](s, optionConverter)
          } else {
            NotFound(errAsJson(NOT_FOUND, "not_found", s"Could not find enterprise with id ${x.id}")).future
          }
          case Failure(ex) =>
            InternalServerError(errAsJson(INTERNAL_SERVER_ERROR, "internal_server_error", s"$ex")).future
        }
        resp
      case (i: InvalidKey) =>
        BadRequest(errAsJson(BAD_REQUEST, "invalid_key", s"invalid id ${i.id}. Check key size[$minKeyLength].")).future
      case _ =>
        BadRequest(errAsJson(BAD_REQUEST, "missing_param", s"No query specified.")).future
    }
    res
  }

  //public api
  @ApiOperation(
    value = "Json response of matching id and date",
    notes = "Invokes a HBase api function to retrieve data by using the date and id param",
    responseContainer = "JSONObject",
    code = 200,
    httpMethod = "GET"
  )
  @ApiResponses(Array(
    new ApiResponse(code = 200, response = classOf[EnterpriseKey], responseContainer = "JsValue", message = "Ok -> Retrieved Enterprise for given id."),
    new ApiResponse(code = 400, responseContainer = "JsValue", message = "BadRequest -> Id or other is invalid."),
    new ApiResponse(code = 404, responseContainer = "JsValue", message = "NotFound -> Given attributes could not be matched."),
    new ApiResponse(code = 500, responseContainer = "JsValue",
      message = "InternalServerError -> Failed to get valid response from endpoint this maybe due to connection timeout or invalid endpoint.")
  ))
  def retrieveEnterprise(
    @ApiParam(value = "Identifier creation date", example = "2017/07", required = true) date: String,
    @ApiParam(value = "An identifier of any type", example = "1244", required = true) id: String
  ): Action[AnyContent] = Action.async { implicit request =>
    /**
     * process params pass both date and id
     */
    val res = matchByParams(Some(id), request, Some(date)) match {
      case (x: ReferencePeriod) =>
        val resp = Try(requestEnterprise.getEnterpriseForReferencePeriod(x.period, x.id)) match {
          case Success(s: Optional[Enterprise]) => if (s.isPresent) {
            resultMatcher[Enterprise](s, optionConverter)
          } else {
            NotFound(errAsJson(NOT_FOUND, "not_found", s"Could not find enterprise with id ${x.id} and period ${x.period}")).future
          }
          case Failure(ex) =>
            InternalServerError(errAsJson(INTERNAL_SERVER_ERROR, "internal_server_error", s"$ex")).future
        }
        resp
      case (y: InvalidReferencePeriod) => BadRequest(errAsJson(BAD_REQUEST, "invalid_date",
        s"cannot parse date with exception ${y.exception}")).future
      case (i: InvalidKey) =>
        BadRequest(errAsJson(BAD_REQUEST, "invalid_key", s"invalid id ${i.id}. Check key size[$minKeyLength].")).future
      case _ =>
        BadRequest(errAsJson(BAD_REQUEST, "missing_param", s"No query specified.")).future

    }
    res
  }

}
