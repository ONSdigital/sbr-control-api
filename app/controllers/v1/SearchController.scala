package controllers.v1

import io.swagger.annotations._
import play.api.mvc.{Action, AnyContent}

import uk.gov.ons.sbr.models.units.{EnterpriseUnit, UnitLinks}

import Services.{DBConnectionInitUtility, DBConnector}


/**
 * Created by haqa on 04/08/2017.
 */

/**
 * @todo - check no-param found err-control
 */
@Api("Search")
class SearchController extends ControllerUtils {

  private val dbInstance: DBConnector = DBConnectionInitUtility.init()

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
    dbInstance.getUnitLinksFromDB(id)
//    val evalResp = matchByParams(Some(id))
//    search[java.util.List[StatisticalUnit]](evalResp, requestLinks.findUnits)
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
    dbInstance.getUnitLinksFromDB(id, date)
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
    dbInstance.getEnterpriseFromDB(id)
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
    dbInstance.getEnterpriseFromDB(id, date)
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
    dbInstance.getStatUnitLinkFromDB(id, category)
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
    dbInstance.getStatUnitLinkFromDB(id, date, category)
  }


}
