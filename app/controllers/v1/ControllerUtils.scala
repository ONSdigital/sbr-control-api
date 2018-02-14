package controllers.v1

import java.time.YearMonth
import java.time.format.{ DateTimeFormatter, DateTimeParseException }
import java.util.Optional
import javax.naming.ServiceUnavailableException

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ Future, TimeoutException }
import scala.util.{ Failure, Success, Try }

import com.typesafe.scalalogging.StrictLogging
import play.api.libs.json._
import play.api.mvc.{ AnyContent, Controller, Request, Result }

import uk.gov.ons.sbr.data.controller.{ EnterpriseController, UnitController }
import uk.gov.ons.sbr.data.domain.{ Enterprise, StatisticalUnit, StatisticalUnitLinks }
import uk.gov.ons.sbr.models.EditEnterprise
import uk.gov.ons.sbr.models.units.{ EnterpriseUnit, KnownUnitLinks, UnitLinks }

import config.Properties.minKeyLength
import utils.Utilities.errAsJson
import utils._
import services.HBaseInMemoryConfig

/**
 * Created by haqa on 10/07/2017.
 */
/**
 * @todo - change Future in resultMatcher
 */
trait ControllerUtils extends Controller with StrictLogging {

  HBaseInMemoryConfig
  protected val requestLinks = new UnitController()
  protected val requestEnterprise = new EnterpriseController()

  protected def validateYearMonth(key: String, raw: String) = {
    val yearAndMonth = Try(YearMonth.parse(raw, DateTimeFormatter.ofPattern("yyyyMM")))
    (yearAndMonth: @unchecked) match {
      case Success(s) =>
        ReferencePeriod(key, s)
      case Failure(ex: DateTimeParseException) =>
        logger.error("cannot parse date to YearMonth object", ex)
        InvalidReferencePeriod(key, ex)
    }
  }

  protected[this] def tryAsResponse(parseToJson: Try[JsValue]): Result = parseToJson match {
    case Success(s) => Ok(s)
    case Failure(ex) =>
      logger.error("Failed to parse instance to expected json format", ex)
      BadRequest(errAsJson(BAD_REQUEST, "bad_request", s"Could not perform action with exception $ex"))
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
    val key = id.getOrElse("")
    if (key.length >= minKeyLength) {
      ((period, request.body.asJson): @unchecked) match {
        case (None, Some(body)) => validateEditEntJson(key, body)
        case (Some(period), Some(body)) => {
          (validateYearMonth(key, period): @unchecked) match {
            case v: ReferencePeriod => validateEditEntJson(key, body, Some(v.period))
            case i: InvalidReferencePeriod => i
          }
        }
      }
    } else { InvalidKey(key) }
  }

  protected def validateEditEntJson(key: String, body: JsValue, period: Option[YearMonth] = None): RequestEvaluation = {
    val placeResult: JsResult[EditEnterprise] = body.validate[EditEnterprise]
    placeResult match {
      case s: JsSuccess[EditEnterprise] => period match {
        case Some(p) => EditRequestByPeriod(key, s.get.updatedBy, p, s.get.updateVars)
        case None => EditRequest(key, s.get.updatedBy, s.get.updateVars)
      }
      case u: JsError => InvalidEditJson(key, u)
    }
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
      case Some(x: java.util.List[StatisticalUnit]) =>
        tryAsResponse(Try(Json.toJson(x.asScala.toList.map { v => UnitLinks(v) })))
      case Some(x: Enterprise) =>
        tryAsResponse(Try(Json.toJson(EnterpriseUnit(x))))
      case Some(x: StatisticalUnitLinks) =>
        tryAsResponse(Try(Json.toJson(KnownUnitLinks(x))))
      case _ =>
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