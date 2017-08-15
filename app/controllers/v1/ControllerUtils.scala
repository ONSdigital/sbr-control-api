package controllers.v1

import java.time.YearMonth
import java.time.format.{ DateTimeFormatter, DateTimeParseException }
import java.util.Optional

import uk.gov.ons.sbr.data.domain.{ Enterprise, StatisticalUnit }
import play.api.mvc.{ AnyContent, Controller, Request, Result }
import com.typesafe.scalalogging.StrictLogging
import play.api.libs.json.JsValue

import scala.util.{ Failure, Success, Try }
import scala.concurrent.Future
import uk.gov.ons.sbr.data.controller._
import uk.gov.ons.sbr.data.hbase.{ HBaseConnector, HBaseTest }
import uk.gov.ons.sbr.models.Links
import uk.gov.ons.sbr.models.units.EnterpriseKey
import utils.Utilities.errAsJson
import utils.Properties.minKeyLength
import utils.{ IdRequest, InvalidKey, InvalidReferencePeriod, ReferencePeriod, RequestEvaluation }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.JavaConversions._

/**
 * Created by haqa on 10/07/2017.
 */
trait ControllerUtils extends Controller with StrictLogging {

  //initialise
  //  HBaseTest.init
  HBaseConnector.getInstance().connect()

  protected val requestLinks = new UnitController()
  protected val requestEnterprise = new EnterpriseController()
  protected val minKeyLength = 4

  //convert date to java format with err handle
  protected def validateYearMonth(key: String, raw: String) = {
    val yearAndMonth = Try(YearMonth.parse(raw, DateTimeFormatter.ofPattern("yyyyMM")))
    // valid date -> check key
    val res: RequestEvaluation = if (key.length >= minKeyLength) {
      yearAndMonth match {
        case Success(s) =>
          ReferencePeriod(key, s)
        case Failure(ex: DateTimeParseException) =>
          logger.error("cannot parse date to YearMonth object", ex)
          InvalidReferencePeriod(key, ex)
      }
    } else { InvalidKey(key) }
    res
  }

  protected[this] def tryAsResponse[T](f: T => JsValue, v: T): Result = Try(f(v)) match {
    case Success(s) => Ok(s)
    case Failure(ex) =>
      logger.error("Failed to parse instance to expected json format", ex)
      BadRequest(errAsJson(BAD_REQUEST, "bad_request", s"Could not perform action ${f.toString} with exception $ex"))
  }

  protected def matchByParams(id: Option[String], request: Request[AnyContent], date: Option[String] = None): RequestEvaluation = {
    // note: request headerAsEmpty when null passed
    val key = id.orElse(request.getQueryString("id")).getOrElse("")
    //    val rawDate = Try(request.getQueryString("date"))
    date match {
      case None => if (key.length >= minKeyLength) { IdRequest(key) } else { InvalidKey(key) }
      case Some(s) =>
        validateYearMonth(key, s)
    }

  }

  protected def optionConverter(o: Optional[Enterprise]): Option[Enterprise] =
    if (o.isPresent) Some(o.get) else None

  protected def toScalaList(l: Optional[java.util.List[StatisticalUnit]]): Option[List[StatisticalUnit]] =
    if (l.isPresent) Some(l.get.toList) else None

  protected def toJavaOptional[A](o: Option[A]): Optional[A] =
    o match { case Some(a) => Optional.ofNullable(a); case _ => Optional.empty[A] }

  /**
   * @note - simplify - AnyRef rep with t.param X
   *
   * @param v - value param to convert
   * @param f - scala conversion function
   * @param msg - overriding msg option
   * @tparam Z - java data type for value param
   * @return Future[Result]
   */
  protected def resultMatcher[Z](v: Optional[Z], f: Optional[Z] => AnyRef,
    msg: Option[String] = None): Future[Result] = {
    Future { f(v) }.map {
      case Some(x: List[StatisticalUnit]) => tryAsResponse[List[StatisticalUnit]](Links.toJson, x)
      case Some(x: Enterprise) => tryAsResponse[Enterprise](EnterpriseKey.toJson, x)
      case None =>
        BadRequest(errAsJson(BAD_REQUEST, "bad_request", msg.getOrElse("Could not parse returned response")))
    }
  }

}
