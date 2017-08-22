package controllers.v1

import java.time.YearMonth
import java.time.format.{ DateTimeFormatter, DateTimeParseException }
import java.util.Optional
import javax.naming.ServiceUnavailableException

import uk.gov.ons.sbr.data.domain.{ Enterprise, StatisticalUnit, UnitType }
import play.api.mvc.{ AnyContent, Controller, Request, Result }
import com.typesafe.scalalogging.StrictLogging
import org.apache.hadoop.util.ToolRunner
import play.api.Logger
import play.api.libs.json.JsValue

import scala.util.{ Failure, Success, Try }
import scala.concurrent.{ Future, TimeoutException }
import uk.gov.ons.sbr.data.controller.{ AdminDataController, EnterpriseController, UnitController }
import uk.gov.ons.sbr.data.hbase.HBaseConnector
import uk.gov.ons.sbr.data.hbase.load.BulkLoader
import uk.gov.ons.sbr.models.UnitLinks
import uk.gov.ons.sbr.models.units.EnterpriseUnit
import utils.Utilities.errAsJson
import utils.Properties.minKeyLength
import utils.{ IdRequest, InvalidKey, InvalidReferencePeriod, ReferencePeriod, RequestEvaluation }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.JavaConversions._

/**
 * Created by haqa on 10/07/2017.
 */
/**
 * @todo - change Future in resultMatcher
 */
trait ControllerUtils extends Controller with StrictLogging {

  Logger.info("Loading local CSVs into In-Memory HBase...")
  val bulkLoader = new BulkLoader()
  val args = List[String](UnitType.ENTERPRISE.toString, "201706", "conf/sample/enterprise.csv")
  ToolRunner.run(HBaseConnector.getInstance().getConfiguration(), bulkLoader, args.toArray)

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
    date match {
      case None => if (key.length >= minKeyLength) { IdRequest(key) } else { InvalidKey(key) }
      case Some(s) =>
        validateYearMonth(key, s)
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
