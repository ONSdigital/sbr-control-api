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

import scala.concurrent.Future
import utils.FutureResponse._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{ Failure, Success, Try }

/**
 * Created by haqa on 04/08/2017.
 */
@Api("Search")
class SearchController @Inject() (db: DataAccess, playConfig: Configuration, langs: Langs, messagesApi: MessagesApi) extends Controller with LazyLogging {

  // Use langs implicitly so we don't have to curry messagesApi("message")(langs) with every use of the messagesApi
  implicit val lang: Lang = langs.availables.head

  // There is probably a more generic way of combining the logic in the two methods below

  def validateStatUnitLinksParams(id: String, category: String, period: String, apply: (String, String, String) => Either[StatUnitLinksParams, InvalidParams]): Either[StatUnitLinksParams, InvalidParams] = apply(id, period, category)

  def validateUnitLinksParams(id: String, apply: (String) => Either[UnitLinksParams, InvalidParams]): Either[UnitLinksParams, InvalidParams] = apply(id)

  def validateEntParams(id: String, period: Option[String], apply: (String, Option[String]) => Either[EnterpriseParams, InvalidParams]): Either[EnterpriseParams, InvalidParams] = apply(id, period)

  def handleValidatedParams(params: Either[ValidParams, InvalidParams]): Future[Result] = params match {
    case Left(v: ValidParams) => v match {
      case u: UnitLinksParams => dbResultMatcher(Try(db.getUnitLinks(u.id)))
      case s: StatUnitLinksParams => dbResultMatcher(Try(db.getStatUnitLinks(s.id, s.category, s.period)))
      case e: EnterpriseParams => dbResultMatcher(Try(db.getEnterprise(e.id, e.period)))
    }
    case Right(i: InvalidParams) => BadRequest(messagesApi(i.msg)).future
  }

  def dbResultMatcher[T](result: Try[Future[Option[T]]]): Future[Result] = result match {
    case Success(s) => s.flatMap(x => x match {
      case Some(a) => a match {
        case e: EnterpriseUnit => Ok(Json.toJson(e)).future
        case u: UnitLinks => Ok(Json.toJson(u)).future
        case l: List[UnitLinks] => Ok(Json.toJson(l)).future
      }
      case None => NotFound(messagesApi("controller.not.found")).future
    })
    case Failure(ex) => {
      logger.error(s"Returned Internal Server Error response with exception", ex)
      InternalServerError(messagesApi("controller.internal.server.error")).future
    }
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
