package controllers.v1

import javax.inject.Inject

import com.typesafe.scalalogging.StrictLogging
import io.swagger.annotations._
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc.{ Action, AnyContent, Result }
import uk.gov.ons.sbr.models.units.{ EnterpriseUnit, UnitLinks }
import services.DataAccess

import scala.concurrent.Future
import utils.FutureResponse._

import scala.util.{ Failure, Success, Try }

/**
 * Created by haqa on 04/08/2017.
 */

// TODO:
// - refactor common apply code into one method, no code duplication
// - move invalid/valid params to /models

// I have to call the companion apply methods applyA not apply because they take the same
// parameters as the normal case class apply method
// http://www.scala-lang.org/old/node/2211

trait ValidParams
case class UnitLinksParams(id: String, period: String) extends ValidParams
object UnitLinksParams {
  def applyA(id: String, period: String): Either[UnitLinksParams, InvalidParams] = {
    Left(UnitLinksParams(id, period))
  }
}

case class EnterpriseParams(id: String, period: String) extends ValidParams
object EnterpriseParams {
  def applyA(id: String, period: String): Either[EnterpriseParams, InvalidParams] = {
    Left(EnterpriseParams(id, period))
  }
}

case class StatUnitLinksParams(id: String, category: String, period: String) extends ValidParams
object StatUnitLinksParams {
  def applyA(id: String, period: String, category: String): Either[StatUnitLinksParams, InvalidParams] = {
    Left(StatUnitLinksParams(id, category, period))
  }
}

trait InvalidParams {
  val msg: String
}
case class InvalidId(msg: String) extends InvalidParams
case class InvalidPeriod(msg: String) extends InvalidParams
case class InvalidIdAndPeriod(msg: String) extends InvalidParams

@Api("Search")
class SearchController @Inject() (db: DataAccess, playConfig: Configuration) extends StrictLogging with ControllerUtils {

  // There is probably a more generic way of combining the logic in the two methods below
  def validateIdPeriodCatParams[T](id: String, period: String, category: String, apply: (String, String, String) => Either[T, InvalidParams]): Either[T, InvalidParams] = apply(id, period, category)

  def validateIdPeriodParams[T](id: String, period: String, apply: (String, String) => Either[T, InvalidParams]): Either[T, InvalidParams] = apply(id, period)

  def handleValidatedParams(params: Either[ValidParams, InvalidParams]): Future[Result] = params match {
    case Left(v: ValidParams) => v match {
      case u: UnitLinksParams => dbResultMatcher(Try(db.getUnitLinks(u.id, u.period)))
      case s: StatUnitLinksParams => dbResultMatcher(Try(db.getStatUnitLinks(s.id, s.category, s.period)))
      case e: EnterpriseParams => dbResultMatcher(Try(db.getEnterprise(e.id, e.period)))
    }
    case Right(i: InvalidParams) => BadRequest(i.msg).future
  }

  def dbResultMatcher[T](result: Try[Option[T]]): Future[Result] = result match {
    case Success(s) => s match {
      case Some(a) => Ok("Ok").future
      case None => NotFound("Not Found").future
    }
    case Failure(ex) => InternalServerError("Internal Server Error").future
  }

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
    handleValidatedParams(validateIdPeriodParams[UnitLinksParams](id, date, UnitLinksParams.applyA))
  }

  @ApiOperation(
    value = "Json response of matching id and date",
    notes = "Invokes a HBase api function to retrieve data by using the date and id param",
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
    @ApiParam(value = "Identifier creation date", example = "2017/07", required = true) date: String,
    @ApiParam(value = "An identifier of any type", example = "1244", required = true) id: String
  ): Action[AnyContent] = Action.async { implicit request =>
    logger.info(s"Received request to get Enterprise with period [$date] and id [$id] parameters.")
    handleValidatedParams(validateIdPeriodParams[EnterpriseParams](id, date, EnterpriseParams.applyA))
  }

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
    @ApiParam(value = "Identifier creation date", example = "2017/07", required = true) date: String,
    @ApiParam(value = "Short word to describe type of id requested", example = "ENT", required = true) category: String,
    @ApiParam(value = "An identifier of any type", example = "1244", required = true) id: String
  ): Action[AnyContent] = Action.async { implicit request =>
    logger.info(s"Received request to get StatisticalUnitLinks with id [$id] and category [$category] parameters.")
    handleValidatedParams(validateIdPeriodCatParams[StatUnitLinksParams](id, date, category, StatUnitLinksParams.applyA))
  }
}
