package controllers.v1

import java.time.YearMonth

import play.api.mvc.{ Action, AnyContent }
import java.util.Optional

import io.swagger.annotations._
import org.apache.hadoop.util.ToolRunner
import play.api.Logger
import uk.gov.ons.sbr.data.controller.AdminDataController
import uk.gov.ons.sbr.data.domain.{ Enterprise, StatisticalUnit, UnitType }
import uk.gov.ons.sbr.data.hbase.HBaseConnector
import uk.gov.ons.sbr.data.hbase.load.BulkLoader
import uk.gov.ons.sbr.models.UnitLinks
import uk.gov.ons.sbr.models.units.EnterpriseUnit

import scala.util.Try
import utils.Utilities.errAsJson

import scala.concurrent.ExecutionContext.Implicits.global
import utils.FutureResponse._
import utils.{ IdRequest, InvalidKey, InvalidReferencePeriod, ReferencePeriod }

/**
 * Created by haqa on 04/08/2017.
 */

/**
 * @todo - generalise and create generic search function x2 per param length -> HList
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
    @ApiParam(value = "An identifier of any type", example = "825039145000", required = true) id: Option[String]
  ): Action[AnyContent] = Action.async { implicit request =>
    val res = matchByParams(id, request) match {
      case (x: IdRequest) =>
        val resp = Try(requestLinks.findUnits(x.id)).futureTryRes.flatMap {
          case (s: Optional[java.util.List[StatisticalUnit]]) =>
            if (s.isPresent) {
              resultMatcher[java.util.List[StatisticalUnit]](s)
            } else {
              logger.debug(s"Could not find entry with id $id")
              NotFound(errAsJson(NOT_FOUND, "not_found", s"Could not find enterprise with id ${x.id}")).future
            }
        } recover responseException
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
    new ApiResponse(code = 200, response = classOf[UnitLinks], responseContainer = "JsValue", message = "Ok -> Retrieved Links for given id."),
    new ApiResponse(code = 400, responseContainer = "JsValue", message = "BadRequest -> Id or other is invalid."),
    new ApiResponse(code = 404, responseContainer = "JsValue", message = "NotFound -> Given attributes could not be matched."),
    new ApiResponse(code = 500, responseContainer = "JsValue",
      message = "InternalServerError -> Failed to get valid response from endpoint this maybe due to connection timeout or invalid endpoint.")
  ))
  def retrieveUnitLinks(
    @ApiParam(value = "Identifier creation date", example = "2017/07", required = true) date: String,
    @ApiParam(value = "An identifier of any type", example = "825039145000", required = true) id: Option[String]
  ): Action[AnyContent] = Action.async { implicit request =>
    val res = matchByParams(id, request, Some(date)) match {
      case (x: ReferencePeriod) =>
        val resp = Try(requestLinks.findUnits(x.period, x.id)).futureTryRes.flatMap {
          case (s) => if (s.isPresent) {
            resultMatcher[java.util.List[StatisticalUnit]](s)
          } else NotFound(errAsJson(NOT_FOUND, "not_found", s"Could not find Unit with id ${x.id} and period ${x.period}")).future
        } recover responseException
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
    new ApiResponse(code = 200, response = classOf[EnterpriseUnit], responseContainer = "JsValue", message = "Ok -> Retrieved Enterprise for given id."),
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
        val resp = Try(requestEnterprise.getEnterprise(x.id)).futureTryRes.flatMap {
          case (s: Optional[Enterprise]) => if (s.isPresent) {
            resultMatcher[Enterprise](s)
          } else NotFound(errAsJson(NOT_FOUND, "not_found", s"Could not find enterprise with id ${x.id}")).future
        } recover responseException
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
    /**
     * process params pass both date and id
     */
    //search[Enterprise, (YearMonth, String)](???, requestEnterprise.getEnterpriseForReferencePeriod)
    val res = matchByParams(Some(id), request, Some(date)) match {
      case (x: ReferencePeriod) =>
        val resp = Try(requestEnterprise.getEnterpriseForReferencePeriod(x.period, x.id)).futureTryRes.flatMap {
          case (s: Optional[Enterprise]) => if (s.isPresent) {
            resultMatcher[Enterprise](s)
          } else NotFound(errAsJson(NOT_FOUND, "not_found", s"Could not find enterprise with id ${x.id} and period ${x.period}")).future
        } recover responseException
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

  //    def search [Z, V][X, Y](r: RequestEvaluation, f: Y => Optional[X]) = {
  //      val res = r match {
  //        case (x: IdRequest) =>
  //  //        requestEnterprise.getEnterprise(x.id)
  //          val resp = Try(f(x.id)).futureTryRes.flatMap {
  //            case (s: Optional[X]) => if (s.isPresent) {
  //              resultMatcher[X](s)
  //            } else NotFound(errAsJson(NOT_FOUND, "not_found", s"Could not find enterprise with id ${x.id}")).future
  //          } recover responseException
  //          resp
  //        case (i: InvalidKey) =>
  //          BadRequest(errAsJson(BAD_REQUEST, "invalid_key", s"invalid id ${i.id}. Check key size[$minKeyLength].")).future
  //        case _ =>
  //          BadRequest(errAsJson(BAD_REQUEST, "missing_param", s"No query specified.")).future
  //      }n
  //      res
  //    }

}
