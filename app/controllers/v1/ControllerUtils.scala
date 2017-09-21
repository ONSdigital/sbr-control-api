package controllers.v1

import java.time.YearMonth
import java.time.format.{ DateTimeFormatter, DateTimeParseException }
import java.util.Optional
import javax.naming.ServiceUnavailableException

import com.fasterxml.jackson.core.JsonParseException
import com.google.inject.Singleton

import scala.util.{ Failure, Success, Try }
import scala.concurrent.{ Future, TimeoutException }
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.JavaConversions._
import play.api.mvc.{ AnyContent, Controller, Request, Result }
import play.api.libs.json.{ JsDefined, JsUndefined, JsValue }
import uk.gov.ons.sbr.data.domain.{ Enterprise, StatisticalUnit }
import uk.gov.ons.sbr.data.controller.{ AdminDataController, EnterpriseController, UnitController }
import uk.gov.ons.sbr.models.units.{ EnterpriseUnit, UnitLinks }
import utils.Utilities.errAsJson
import utils._
import config.Properties.minKeyLength

/**
 * Created by haqa on 10/07/2017.
 */
/**
 * @todo - change Future in resultMatcher
 */
trait ControllerUtils extends Controller with StrictLogging {

  InMemoryInit
  protected val requestLinks = new UnitController()
  protected val requestEnterprise = new EnterpriseController()

  private def validateYearMonth(key: String, raw: String) = {
    val yearAndMonth = Try(YearMonth.parse(raw, DateTimeFormatter.ofPattern("yyyyMM")))
    yearAndMonth match {
      case Success(s) =>
        ReferencePeriod(key, s)
      case Failure(ex: DateTimeParseException) =>
        logger.error("cannot parse date to YearMonth object", ex)
        InvalidReferencePeriod(key, ex)
    }
  }

  protected[this] def tryAsResponse[T](f: T => JsValue, v: T): Result = Try(f(v)) match {
    case Success(s) => Ok(s)
    case Failure(ex) =>
      logger.error("Failed to parse instance to expected json format", ex)
      BadRequest(errAsJson(BAD_REQUEST, "bad_request", s"Could not perform action ${f.toString} with exception $ex"))
  }

  protected def matchByParams(id: Option[String], request: Request[AnyContent], date: Option[String] = None): RequestEvaluation = {
    val key = id.orElse(request.getQueryString("id")).getOrElse("")
    if (key.length >= minKeyLength) {
      date match {
        case None => IdRequest(key)
        case Some(s) => validateYearMonth(key, s)
      }
    } else { InvalidKey(key) }
  }

  protected def matchByEditParams(id: Option[String], request: Request[AnyContent], period: Option[String] = None): RequestEvaluation = {
    val key = id.orElse(request.getQueryString("id")).getOrElse("")
    if (key.length >= minKeyLength) {
      (period, request.body.asJson) match {
        case (None, Some(body)) => Try(parseBodyJson(body)) match {
          case Success(s) => EditRequest(key, s._1, s._2)
          case Failure(ex: JsonParseException) => InvalidEditJson(key, ex)
        }
        case (Some(period), Some(body)) => (Try(parseYearMonth(period)), Try(parseBodyJson(body))) match {
          case (Success(period), Success(body)) => EditRequestByPeriod(key, body._1, period, body._2)
          case (Failure(ex: DateTimeParseException), _) => InvalidReferencePeriod(key, ex)
          case (_, Failure(ex: JsonParseException)) => InvalidEditJson(key, ex)
        }
      }
    } else { InvalidKey(key) }
  }

  protected def parseBodyJson(body: JsValue): (String, Map[String, String]) = {
    val updateVars = (body \ "vars").as[Map[String, String]]
    val updatedBy = (body \ "updatedBy").as[String]
    (updatedBy, updateVars)
  }

  private def parseYearMonth(raw: String): YearMonth = {
    YearMonth.parse(raw, DateTimeFormatter.ofPattern("yyyyMM"))
  }

  protected def toOption[X](o: Optional[X]) = if (o.isPresent) Some(o.get) else None

  protected def toJavaOptional[A](o: Option[A]): Optional[A] =
    o match { case Some(a) => Optional.ofNullable(a); case _ => Optional.empty[A] }

  /**
   * @note - simplify - AnyRef rep with t.param X
   *
   * @param v - value param to convert
   * @param msg - overriding msg option
   * @tparam Z - java data type for value param
   * @return Future[Result]
   */
  protected def resultMatcher[Z](v: Optional[Z], msg: Option[String] = None): Future[Result] = {
    Future { toOption[Z](v) }.map {
      case Some(x: java.util.List[StatisticalUnit]) => tryAsResponse[List[StatisticalUnit]](UnitLinks.toJson, x.toList)
      case Some(x: Enterprise) => tryAsResponse[Enterprise](EnterpriseUnit.toJson, x)
      case None =>
        BadRequest(errAsJson(BAD_REQUEST, "bad_request", msg.getOrElse("Could not parse returned response")))
    }
  }

  protected def responseException: PartialFunction[Throwable, Result] = {
    case ex: DateTimeParseException =>
      BadRequest(errAsJson(BAD_REQUEST, "invalid_date", s"cannot parse date exception found $ex"))
    case ex: RuntimeException => InternalServerError(errAsJson(INTERNAL_SERVER_ERROR, "runtime_exception", s"$ex"))
    case ex: ServiceUnavailableException =>
      ServiceUnavailable(errAsJson(SERVICE_UNAVAILABLE, "service_unavailable", s"$ex"))
    case ex: TimeoutException =>
      RequestTimeout(errAsJson(REQUEST_TIMEOUT, "request_timeout", s"This may be due to connection being blocked. $ex"))
    case ex => InternalServerError(errAsJson(INTERNAL_SERVER_ERROR, "internal_server_error", s"$ex."))
  }

}
