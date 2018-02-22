package controllers.v1

import java.time.YearMonth
import java.time.format.{ DateTimeFormatter, DateTimeParseException }

import scala.util.{ Failure, Success, Try }

import com.typesafe.scalalogging.StrictLogging
import play.api.libs.json._
import play.api.mvc.{ AnyContent, Controller, Request }

import uk.gov.ons.sbr.models.EditEnterprise

import config.Properties.minKeyLength
import utils._

/**
 * Created by haqa on 10/07/2017.
 */
/**
 * @todo - change Future in resultMatcher
 */
trait ControllerUtils extends Controller with StrictLogging {

  protected def validateYearMonth(key: String, raw: String) = {
    val yearAndMonth = Try(YearMonth.parse(raw, DateTimeFormatter.ofPattern("yyyyMM")))
    yearAndMonth match {
      case Success(s) =>
        ReferencePeriod(key, s)
      case Failure(ex: DateTimeParseException) =>
        logger.error("cannot parse date to YearMonth object", ex)
        InvalidReferencePeriod(key, ex)
    }
  }

  protected def matchByEditParams(id: Option[String], request: Request[AnyContent], period: Option[String] = None): RequestEvaluation = {
    val key = id.getOrElse("")
    if (key.length >= minKeyLength) {
      (period, request.body.asJson) match {
        case (None, Some(body)) => validateEditEntJson(key, body)
        case (Some(period), Some(body)) => {
          validateYearMonth(key, period) match {
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
}