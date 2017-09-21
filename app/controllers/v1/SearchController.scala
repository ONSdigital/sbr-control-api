package controllers.v1

import java.time.YearMonth
import java.util.Optional

import io.swagger.annotations._

import scala.util.Try
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.mvc.{ Action, AnyContent, Result }
import uk.gov.ons.sbr.data.domain.{ Enterprise, StatisticalUnit, StatisticalUnitLinks, UnitType }
import uk.gov.ons.sbr.models.units.{ EnterpriseUnit, UnitLinks }
import utils.FutureResponse.{ futureFromTry, futureSuccess }
import utils.Utilities.errAsJson
import utils._
import config.Properties.minKeyLength

/**
 * Created by haqa on 04/08/2017.
 */

/**
 * @todo
 *       - check no-param found err-control
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
    new ApiResponse(code = 200, response = classOf[UnitLinks], responseContainer = "JsValue", message = "Ok -> Retrieved Links for given id."),
    new ApiResponse(code = 400, responseContainer = "JsValue", message = "BadRequest -> Id or other is invalid."),
    new ApiResponse(code = 404, responseContainer = "JsValue", message = "NotFound -> Given attributes could not be matched."),
    new ApiResponse(code = 500, responseContainer = "JsValue",
      message = "InternalServerError -> Failed to get valid response from endpoint this maybe due to connection timeout or invalid endpoint.")
  ))
  def retrieveUnitLinksById(
    @ApiParam(value = "An identifier of any type", example = "825039145000", required = true) id: String
  ): Action[AnyContent] = Action.async { implicit request =>
    val evalResp = matchByParams(Some(id), request)
    search[java.util.List[StatisticalUnit]](evalResp, requestLinks.findUnits)
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
    new ApiResponse(code = 200, response = classOf[UnitLinks], responseContainer = "JsValue", message = "Ok -> Retrieved Links for given id."),
    new ApiResponse(code = 400, responseContainer = "JsValue", message = "BadRequest -> Id or other is invalid."),
    new ApiResponse(code = 404, responseContainer = "JsValue", message = "NotFound -> Given attributes could not be matched."),
    new ApiResponse(code = 500, responseContainer = "JsValue",
      message = "InternalServerError -> Failed to get valid response from endpoint this maybe due to connection timeout or invalid endpoint.")
  ))
  def retrieveUnitLinks(
    @ApiParam(value = "Identifier creation date", example = "2017/07", required = true) date: String,
    @ApiParam(value = "An identifier of any type", example = "825039145000", required = true) id: String
  ): Action[AnyContent] = Action.async { implicit request =>
    val evalResp = matchByParams(Some(id), request, Some(date))
    searchByTwoParams[java.util.List[StatisticalUnit], YearMonth, String](evalResp, requestLinks.findUnits)
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
    new ApiResponse(code = 200, response = classOf[EnterpriseUnit], responseContainer = "JsValue", message = "Ok -> Retrieved Enterprise for given id."),
    new ApiResponse(code = 400, responseContainer = "JsValue", message = "BadRequest -> Id or other is invalid."),
    new ApiResponse(code = 404, responseContainer = "JsValue", message = "NotFound -> Given attributes could not be matched."),
    new ApiResponse(code = 500, responseContainer = "JsValue",
      message = "InternalServerError -> Failed to get valid response from endpoint this maybe due to connection timeout or invalid endpoint.")
  ))
  def retrieveEnterpriseById(
    @ApiParam(value = "An identifier of any type", example = "1244", required = true) id: String
  ): Action[AnyContent] = Action.async { implicit request =>
    val evalResp = matchByParams(Some(id), request)
    search[Enterprise](evalResp, requestEnterprise.getEnterprise)
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
    new ApiResponse(code = 200, response = classOf[EnterpriseUnit], responseContainer = "JsValue", message = "Ok -> Retrieved Enterprise for given id."),
    new ApiResponse(code = 400, responseContainer = "JsValue", message = "BadRequest -> Id or other is invalid."),
    new ApiResponse(code = 404, responseContainer = "JsValue", message = "NotFound -> Given attributes could not be matched."),
    new ApiResponse(code = 500, responseContainer = "JsValue",
      message = "InternalServerError -> Failed to get valid response from endpoint this maybe due to connection timeout or invalid endpoint.")
  ))
  def retrieveEnterprise(
    @ApiParam(value = "Identifier creation date", example = "2017/07", required = true) date: String,
    @ApiParam(value = "An identifier of any type", example = "1244", required = true) id: String
  ): Action[AnyContent] = Action.async { implicit request =>
    val evalResp = matchByParams(Some(id), request, Some(date))
    searchByTwoParams[Enterprise, YearMonth, String](evalResp, requestEnterprise.getEnterpriseForReferencePeriod)
  }

  //public api
  @ApiOperation(
    value = "Retrieves Unit links using id and unit type",
    notes = "This function assumes the user knowns the type of id and requires unlike the previous functions.",
    responseContainer = "JSONObject",
    code = 200,
    httpMethod = "GET"
  )
  @ApiResponses(Array(
    new ApiResponse(code = 200, response = classOf[UnitLinks], responseContainer = "JsValue", message = "Ok -> Retrieved Enterprise for given id."),
    new ApiResponse(code = 400, responseContainer = "JsValue", message = "BadRequest -> Id or other is invalid."),
    new ApiResponse(code = 404, responseContainer = "JsValue", message = "NotFound -> Given attributes could not be matched."),
    new ApiResponse(code = 500, responseContainer = "JsValue",
      message = "InternalServerError -> Failed to get valid response from endpoint this maybe due to connection timeout or invalid endpoint.")
  ))
  def retrieveStatUnitLinks(
    @ApiParam(value = "Short word to describe type of id requested", example = "ENT", required = true) category: String,
    @ApiParam(value = "An identifier of any type", example = "1244", required = true) id: String
  ): Action[AnyContent] = Action.async { implicit request =>
    val idValidation = matchByParams(Some(id), request, None)
    val evalResp = idValidation match {
      case (x: IdRequest) => CategoryRequest(x.id, UnitType.fromString(category))
      case z => z
    }
    searchByTwoParams[StatisticalUnitLinks, String, UnitType](evalResp, requestLinks.getUnitLinks)
  }

  //public api
  @ApiOperation(
    value = "Retrieves Unit links using id, period and unit type",
    notes = "This function assumes the user knows the type of id and requires unlike the previous functions.",
    responseContainer = "JSONObject",
    code = 200,
    httpMethod = "GET"
  )
  @ApiResponses(Array(
    new ApiResponse(code = 200, response = classOf[UnitLinks], responseContainer = "JsValue", message = "Ok -> Retrieved Enterprise for given id."),
    new ApiResponse(code = 400, responseContainer = "JsValue", message = "BadRequest -> Id or other is invalid."),
    new ApiResponse(code = 404, responseContainer = "JsValue", message = "NotFound -> Given attributes could not be matched."),
    new ApiResponse(code = 500, responseContainer = "JsValue",
      message = "InternalServerError -> Failed to get valid response from endpoint this maybe due to connection timeout or invalid endpoint.")
  ))
  def retrieveStatUnitLinksWithPeriod(
    @ApiParam(value = "Keyword describing type of id requested", example = "ENT", required = true) category: String,
    @ApiParam(value = "Identifier creation date", example = "2017/07", required = true) date: String,
    @ApiParam(value = "An identifier of any type", example = "1244", required = true) id: String
  ): Action[AnyContent] = Action.async { implicit request =>
    // validate id and date to get valid reference period -> add category in
    matchByParams(Some(id), request, Some(date)) match {
      case (x: ReferencePeriod) =>
        val resp = Try(requestLinks.getUnitLinks(x.period, x.id, UnitType.fromString(category))).futureTryRes.flatMap {
          case (s: Optional[StatisticalUnitLinks]) => if (s.isPresent) {
            resultMatcher[StatisticalUnitLinks](s)
          } else NotFound(errAsJson(NOT_FOUND, "not_found", s"Could not find unit link with " +
            s"id ${x.id}, period ${x.period} and grouping $category")).future
        } recover responseException
        resp
      case x => invalidSearchResponses(x)
    }
  }

  private def search[X](eval: RequestEvaluation, funcWithId: String => Optional[X]): Future[Result] = {
    val res = eval match {
      case (x: IdRequest) =>
        val resp = Try(funcWithId(x.id)).futureTryRes.flatMap {
          case (s: Optional[X]) => if (s.isPresent) {
            resultMatcher[X](s)
          } else NotFound(errAsJson(NOT_FOUND, "not_found", s"Could not find enterprise with id ${x.id}")).future
        } recover responseException
        resp
      case _ => invalidSearchResponses(eval)
    }
    res
  }

  //  @todo - combine ReferencePeriod and UnitGrouping
  private def searchByTwoParams[X, Z, Y](eval: RequestEvaluation, funcWithIdAndParam: (Z, Y) => Optional[X]): Future[Result] = {
    val res = eval match {
      case (x: ReferencePeriod) =>
        val resp = Try(funcWithIdAndParam(x.period, x.id)).futureTryRes.flatMap {
          case (s: Optional[X]) => if (s.isPresent) {
            resultMatcher[X](s)
          } else NotFound(errAsJson(NOT_FOUND, "not_found", s"Could not find enterprise with id ${x.id} and period ${x.period}")).future
        } recover responseException
        resp
      case (x: CategoryRequest) =>
        val resp = Try(funcWithIdAndParam(x.category, x.id)).futureTryRes.flatMap {
          case (s: Optional[X]) => if (s.isPresent) {
            resultMatcher[X](s)
          } else NotFound(errAsJson(NOT_FOUND, "not_found", s"Could not find enterprise with id ${x.id} and grouping ${x.category}")).future
        } recover responseException
        resp
      case _ => invalidSearchResponses(eval)
    }
    res
  }

  private def invalidSearchResponses(invalidRequest: RequestEvaluation) = {
    invalidRequest match {
      case (y: InvalidReferencePeriod) => BadRequest(errAsJson(BAD_REQUEST, "invalid_date",
        s"cannot parse date with exception ${y.exception}")).future
      case (i: InvalidKey) =>
        BadRequest(errAsJson(BAD_REQUEST, "invalid_key", s"invalid id ${i.id}. Check key size[$minKeyLength].")).future
      case _ =>
        BadRequest(errAsJson(BAD_REQUEST, "missing_param", s"No query specified.")).future
    }
  }

}
