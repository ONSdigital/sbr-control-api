package controllers.v1

import javax.inject.Inject

import com.typesafe.scalalogging.LazyLogging
import io.swagger.annotations._

import play.api.Configuration
import play.api.i18n.{ Lang, Langs, MessagesApi }
import play.api.libs.json.Json
import play.api.mvc.{ Action, AnyContent, Controller, Result }

import services.DataAccess
import uk.gov.ons.sbr.models._
import uk.gov.ons.sbr.models.units._
import utils.FutureResponse._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Api("Search")
class SearchController @Inject() (db: DataAccess, playConfig: Configuration, langs: Langs, messagesApi: MessagesApi) extends Controller with LazyLogging {

  // Use langs implicitly so we don't have to curry messagesApi("message")(langs) with every use of the messagesApi
  implicit val lang: Lang = langs.availables.head

  def validateStatUnitLinksParams(id: String, category: String, period: String, apply: (String, String, String) => Either[InvalidParams, StatUnitLinksParams]): Either[InvalidParams, StatUnitLinksParams] = apply(id, category, period)

  def validateUnitLinksParams(id: String, apply: (String) => Either[InvalidParams, UnitLinksParams]): Either[InvalidParams, UnitLinksParams] = apply(id)

  def validateEntParams(id: String, period: Option[String], apply: (String, Option[String]) => Either[InvalidParams, EnterpriseParams]): Either[InvalidParams, EnterpriseParams] = apply(id, period)

  def handleValidatedParams(params: Either[InvalidParams, ValidParams]): Future[Result] = params match {
    case Right(v: ValidParams) => v match {
      case u: UnitLinksParams => dbResultMatcher(db.getUnitLinks(u.id))
      case s: StatUnitLinksParams => dbResultMatcher(db.getStatUnitLinks(s.id, s.category, s.period))
      case e: EnterpriseParams => dbResultMatcher(db.getEnterprise(e.id, e.period))
    }
    case Left(i: InvalidParams) => BadRequest(messagesApi(i.msg)).future
  }

  def dbResultMatcher(result: Future[DbResponse]): Future[Result] = result.map(x => x match {
    case b: DbSuccessUnitLinks => Ok(Json.toJson(b.result))
    case a: DbSuccessEnterprise => Ok(Json.toJson(a.result))
    case c: DbSuccessUnitLinksList => Ok(Json.toJson(c.result))
    case e: DbNotFound => NotFound(messagesApi("controller.not.found"))
    case f: DbServerError => dbError(f)
    case g: DbTimeout => dbError(g)
  })

  def dbError(failure: DbErrorMsg): Result = {
    logger.error(s"Returned Internal Server Error response with DbFailure [$failure]: ${failure.msg}")
    InternalServerError(messagesApi("controller.internal.server.error"))
  }

  @ApiOperation(
    value = "Json response of matching id and date",
    notes = "Invokes a HBase api function to retrieve data by using the date (optional) and id param",
    responseContainer = "JSONObject",
    code = 200,
    httpMethod = "GET"
  )
  @ApiResponses(Array(
    new ApiResponse(code = 200, response = classOf[EnterpriseUnit], responseContainer = "JsValue",
      message = "Ok -> Retrieved Enterprise for given id."),
    new ApiResponse(code = 400, responseContainer = "JsValue", message = "BadRequest -> Id or other is invalid."),
    new ApiResponse(code = 404, responseContainer = "JsValue", message = "NotFound -> Given attributes could not be matched."),
    new ApiResponse(code = 500, responseContainer = "JsValue",
      message = "InternalServerError -> Failed to get valid response from endpoint this maybe due to connection timeout or invalid endpoint.")
  ))
  def retrieveEnterprise(
    @ApiParam(value = "Identifier creation date", example = "201707", required = false) date: Option[String],
    @ApiParam(value = "An identifier of any type", example = "12345", required = true) id: String
  ): Action[AnyContent] = Action.async { implicit request =>
    logger.info(s"Received request to get Enterprise with period [$date] and id [$id] parameters.")
    handleValidatedParams(validateEntParams(id, date, EnterpriseParams.validate))
  }

  @ApiOperation(
    value = "Json response of links that correspond to an id and period.",
    notes = "Gets the links for a specific id, which could include conflicts so multiple results may be returned.",
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
    @ApiParam(value = "An identifier of any type", example = "825039145000", required = true) id: String
  ): Action[AnyContent] = Action.async { implicit request =>
    logger.info(s"Received request to get a List of StatUnitLinks with id [$id] parameters.")
    handleValidatedParams(validateUnitLinksParams(id, UnitLinksParams.validate))
  }

  @ApiOperation(
    value = "Retrieves Unit links using id, unit type and period.",
    notes = "This endpoint assumes the user knows the type of id, in addition to the id and period.",
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
    @ApiParam(value = "Identifier creation date", example = "2017/07", required = true) date: String,
    @ApiParam(value = "Short word to describe type of id requested", example = "ENT", required = true) category: String,
    @ApiParam(value = "An identifier of any type", example = "1244", required = true) id: String
  ): Action[AnyContent] = Action.async { implicit request =>
    logger.info(s"Received request to get StatisticalUnitLinks with id [$id] and category [$category] parameters.")
    handleValidatedParams(validateStatUnitLinksParams(id, category, date, StatUnitLinksParams.validate))
  }
}
