package controllers.v1

import java.time.YearMonth
import java.util.Optional

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

import io.swagger.annotations._
import play.api.mvc.{Action, AnyContent, Result}

import uk.gov.ons.sbr.data.domain.{Enterprise, StatisticalUnit, StatisticalUnitLinks, UnitType}
import uk.gov.ons.sbr.models.units.{EnterpriseUnit, UnitLinks}

import config.Properties.minKeyLength
import utils.FutureResponse.{futureFromTry, futureSuccess}
import utils.Utilities.errAsJson
import utils._

/**
  * Created by haqa on 04/08/2017.
  */

/**
  * @todo - check no-param found err-control
  */
@Api("Search")
class SearchControllerOld extends ControllerUtils {

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
    logger.info(s"Received request to get a List of Unit Links with id [$id] parameters.")
    val evalResp = matchByParams(Some(id))
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
    logger.info(s"Received request to get a List of StatisticalUnits with period [$date] and id [$id] parameters.")
    val evalResp = matchByParams(Some(id), Some(date))
    searchByPeriod[java.util.List[StatisticalUnit]](evalResp, requestLinks.findUnits)
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
    logger.info(s"Received request to get Enterprise with id [$id] parameters.")
    val evalResp = matchByParams(Some(id))
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
    logger.info(s"Received request to get Enterprise with period [$date] and id [$id] parameters.")
    val evalResp = matchByParams(Some(id), Some(date))
    searchByPeriod[Enterprise](evalResp, requestEnterprise.getEnterpriseForReferencePeriod)
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
    logger.info(s"Received request to get StatisticalUnitLinks with id [$id] and category [$category] parameters.")
    val idValidation = matchByParams(Some(id), None)
    val evalResp = idValidation match {
      case (x: IdRequest) =>
        CategoryRequest(x.id, UnitType.fromString(category))
      case z => z
    }
    val res = evalResp match {
      case (x: CategoryRequest) =>
        val resp = Try(requestLinks.getUnitLinks(x.id, x.category)).futureTryRes.flatMap {
          case (s: Optional[StatisticalUnitLinks]) => if (s.isPresent) {
            resultMatcher[StatisticalUnitLinks](s)
          } else NotFound(errAsJson(NOT_FOUND, "not_found",
            s"Could not find enterprise with id ${x.id} and grouping ${x.category}")).future
        } recover responseException
        resp
      case _ => invalidSearchResponses(evalResp)
    }
    res
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
                                       @ApiParam(value = "Identifier creation date", example = "2017/07", required = true) date: String,
                                       @ApiParam(value = "Keyword describing type of id requested", example = "ENT", required = true) category: String,
                                       @ApiParam(value = "An identifier of any type", example = "1244", required = true) id: String
                                     ): Action[AnyContent] = Action.async { implicit request =>
    logger.info(s"Received request to get StatisticalUnitLinks with period [$date], id [$id] and category [$category] parameters.")
    matchByParams(Some(id), Some(date)) match {
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
  private def searchByPeriod[X](eval: RequestEvaluation, funcWithIdAndParam: (YearMonth, String) => Optional[X]): Future[Result] = {
    val res = eval match {
      case (x: ReferencePeriod) =>
        val resp = Try(funcWithIdAndParam(x.period, x.id)).futureTryRes.flatMap {
          case (s: Optional[X]) => if (s.isPresent) {
            resultMatcher[X](s)
          } else NotFound(errAsJson(NOT_FOUND, "not_found",
            s"Could not find enterprise with id ${x.id} and period ${x.period}")).future
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
        BadRequest(errAsJson(BAD_REQUEST, "invalid_key",
          s"invalid id ${i.id}. Check key size[$minKeyLength].")).future
      case _ =>
        BadRequest(errAsJson(BAD_REQUEST, "missing_param", s"No query specified.")).future
    }
  }

}
