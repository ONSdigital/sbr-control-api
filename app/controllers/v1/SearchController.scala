package controllers.v1

import play.api.mvc.{ Action, AnyContent }
import java.time.{ DateTimeException, YearMonth }
import java.util.Optional

import io.swagger.annotations._
import uk.gov.ons.sbr.data.domain.{ Enterprise, Unit }

import scala.util.Try
import utils.Utilities.errAsJson

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
  ): Action[AnyContent] = Action { implicit request =>
    val key: String = Try(getQueryString(request, "id")).getOrElse("")
    val tree = requestLinks.findUnits(key)
    tree
    NoContent
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
      case (x: String, Some(y: YearMonth)) =>
        val resp: Optional[java.util.List[Unit]] = requestLinks.findUnits(y, x)
        resultMatcher[java.util.List[Unit]](resp, toScalaList, None)
      case (_, None) => futureResult(BadRequest(errAsJson(BAD_REQUEST, "bad_request",
        s"cannot_parse_date with exception ${new DateTimeException("could not parse date to YearMonth")}")))
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
    println(s"Your key is: ${key}")
    val resp = requestEnterprise.getEnterprise(key)
    val enterprise = resultMatcher[Enterprise](resp, optionConverter, None)
    enterprise
    //    NoContent
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
      case (x: String, Some(y: YearMonth)) =>
        // need a try and catch here
        val resp: Optional[Enterprise] = requestEnterprise.getEnterprise(y, x)
        resultMatcher[Enterprise](resp, optionConverter, None)
      case (_, None) => futureResult(BadRequest(errAsJson(BAD_REQUEST, "bad_request",
        s"cannot_parse_date with exception ${new DateTimeException("could not parse date to YearMonth")}")))
    }
    res
  }

}
