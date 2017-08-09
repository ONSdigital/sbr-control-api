package controllers.v1

import play.api.mvc.{ Action, AnyContent }
import java.util.Optional

import io.swagger.annotations._
import uk.gov.ons.sbr.data.domain.{ Enterprise, StatisticalUnit }
import utils.{ IdRequest, InvalidReferencePeriod, ReferencePeriod }

import scala.util.{ Failure, Success, Try }
import utils.Utilities.errAsJson
import utils.Properties.minKeyLength

/**
 * Created by haqa on 04/08/2017.
 */
@Api("Search")
class SearchController extends ControllerUtils {

  //public api
  @ApiOperation(
    value = "Json response of links that correspond to id",
    notes = "Invokes a HBase api function to retrieve a nested link of potential parent and children by using the id param",
    responseContainer = "JSONObject",
    code = 200,
    httpMethod = "GET"
  )
  @ApiResponses(Array(
    //    new ApiResponse(code = 200, response = classOf[Enterprise], responseContainer = "JSONObject", message = "Success -> Record(s) found for id."),
    //    new ApiResponse(code = 400, responseContainer = "JSONObject", message = "Client Side Error -> Required parameter was not found.")
    new ApiResponse(code = 200, responseContainer = "JsValue", message = "Success -> Retrieved Links for given id.")
  ))
  def retrieveLinksById(
    @ApiParam(value = "An identifier of any type", example = "825039145000", required = true) id: Option[String]
  ): Action[AnyContent] = Action.async { implicit request =>
    val key: String = Try(getQueryString(request, "id")).getOrElse("")
    val res = key match {
      case key if key.length >= minKeyLength =>
        Try(requestLinks.findUnits(key)) match {
          case Success(s) => if (s.isPresent) {
            resultMatcher[java.util.List[StatisticalUnit]](s, toScalaList)
          } else {
            futureResult(NotFound(errAsJson(NOT_FOUND, "not_found", s"Could not find enterprise with id $key")))
          }
          case Failure(ex) =>
            futureResult(InternalServerError(errAsJson(INTERNAL_SERVER_ERROR, "internal_server_error", s"$ex")))
        }
      case _ => futureResult(BadRequest(errAsJson(400, "missing_query", "No query specified.")))
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
    new ApiResponse(code = 200, responseContainer = "JsValue", message = "Success -> Retrieved Links for given id and date.")
  ))
  def retrieveLinks(
    @ApiParam(value = "Identifier creation date", example = "2017/11", required = true) date: Option[String],
    @ApiParam(value = "An identifier of any type", example = "825039145000", required = true) id: Option[String]
  ): Action[AnyContent] = Action.async { implicit request =>
    val res = unpackParams(request) match {
      case (x: ReferencePeriod) =>
        val resp = Try(requestLinks.findUnits(x.period, x.id)) match {
          case Success(s) => if (s.isPresent) {
            resultMatcher[java.util.List[StatisticalUnit]](s, toScalaList)
          } else {
            futureResult(NotFound(errAsJson(NOT_FOUND, "not_found", s"Could not find enterprise with id ${x.id}")))
          }
          case Failure(ex) =>
            futureResult(InternalServerError(errAsJson(INTERNAL_SERVER_ERROR, "internal_server_error", s"$ex")))
        }
        resp
      case (e: InvalidReferencePeriod) => futureResult(BadRequest(errAsJson(BAD_REQUEST, "bad_request",
        s"cannot parse date with exception ${e.exception}")))
      case (_: IdRequest) => futureResult(BadRequest(errAsJson(400, "missing_query", "No query specified.")))

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
    new ApiResponse(code = 200, responseContainer = "JsValue", message = "Success -> Record found for given id.")
  ))
  def retrieveEnterpriseById(id: Option[String]): Action[AnyContent] = Action.async { implicit request =>
    val key: String = Try(getQueryString(request, "id")).getOrElse("")
    val res = key match {
      case key if key.length >= minKeyLength =>
        Try(requestEnterprise.getEnterprise(key)) match {
          case Success(s: Optional[Enterprise]) => if (s.isPresent) {
            resultMatcher[Enterprise](s, optionConverter)
          } else {
            futureResult(NotFound(errAsJson(NOT_FOUND, "not_found", s"Could not find enterprise with id $key")))
          }
          case Failure(ex) =>
            futureResult(InternalServerError(errAsJson(INTERNAL_SERVER_ERROR, "internal_server_error", s"$ex")))
        }
      case _ =>
        futureResult(BadRequest(errAsJson(400, "missing_query", "No query specified.")))
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
    new ApiResponse(code = 200, responseContainer = "JsValue", message = "Success -> Record found for given id and date.")
  ))
  def retrieveEnterprise(
    @ApiParam(value = "Identifier creation date", example = "2017/11", required = true) date: Option[String],
    @ApiParam(value = "An identifier of any type", example = "825039145000", required = true) id: Option[String]
  ): Action[AnyContent] = Action.async { implicit request =>
    val res = unpackParams(request) match {
      case (x: ReferencePeriod) =>
        val resp = Try(requestEnterprise.getEnterpriseForReferencePeriod(x.period, x.id)) match {
          case Success(s: Optional[Enterprise]) => if (s.isPresent) {
            resultMatcher[Enterprise](s, optionConverter)
          } else {
            futureResult(NotFound(errAsJson(NOT_FOUND, "not_found", s"Could not find enterprise with id ${x.id}")))
          }
          case Failure(ex) =>
            futureResult(InternalServerError(errAsJson(INTERNAL_SERVER_ERROR, "internal_server_error", s"$ex")))
        }
        resp
      case (e: InvalidReferencePeriod) => futureResult(BadRequest(errAsJson(BAD_REQUEST, "bad_request",
        s"cannot parse date with exception ${e.exception}")))
      case (_: IdRequest) => futureResult(BadRequest(errAsJson(400, "missing_query", "No query specified.")))

    }
    res
  }

}
