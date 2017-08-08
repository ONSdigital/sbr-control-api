package controllers.v1

import java.time.YearMonth
import java.time.format.{ DateTimeFormatter, DateTimeParseException }
import java.util.{ Optional }
import uk.gov.ons.sbr.data.domain.{ Unit, Enterprise }

import play.api.mvc.{ AnyContent, Controller, Request, Result }
import com.typesafe.scalalogging.StrictLogging

import scala.util.{ Failure, Success, Try }
import scala.concurrent.{ ExecutionContext, Future }
import uk.gov.ons.sbr.data.controller.UnitController
import uk.gov.ons.sbr.data.controller.EnterpriseController
import utils.Utilities.errAsJson

import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.JavaConversions._

/**
 * Created by haqa on 10/07/2017.
 */
trait ControllerUtils extends Controller with StrictLogging {

  protected val requestLinks = new UnitController()
  protected val requestEnterprise = new EnterpriseController()

  protected def getQueryString(request: Request[AnyContent], elem: String): String = request.getQueryString(elem).getOrElse("")

  protected[this] def resultAsResponse(f: => Future[Result]): Future[Result] = Try(f) match {
    case Success(g) => g
    case Failure(err) =>
      logger.error("Unable to produce response.", err)
      Future.successful {
        InternalServerError(s"{err = '${err}'}")
      }
  }

  protected def unpackParams(request: Request[AnyContent]) = {
    val key: String = Try(getQueryString(request, "id")).getOrElse("")
    val rawDate: String = Try(getQueryString(request, "date")).getOrElse("")
    val yearAndMonth = Try(YearMonth.parse(rawDate, DateTimeFormatter.ofPattern("yyyyMM")))
    yearAndMonth match {
      case Success(s) => key -> Some(s)
      case Failure(ex: DateTimeParseException) =>
        logger.error("cannot parse date to YearMonth object", ex)
        key -> None
    }
  }

  protected def futureResult(r: Result) = Future.successful(r)

  protected def futureErr(ex: Exception) = Future.failed(ex)

  protected def futureTryRes[T](f: Try[T]) = Future.fromTry(f)

  @deprecated("Moved to resultMatcher with f param", "feature/data-retrieval [Tue 8 Aug 2017 - 11:35]")
  protected def resultMatcher[Z](v: Optional[Enterprise], msg: Option[String])(implicit ec: ExecutionContext): Future[Result] = {
    Future {
      optionConverter(v)
    }.map {
      case Some(x) => Ok("")
      case None => NotFound(errAsJson(NOT_FOUND, "bad_request", s"${msg.getOrElse("Could not find requested id")}"))
    }
  }

  /**
   * @note - simplify - get rid of AnyRef
   *
   * @param v - value param to convert
   * @param f - scala conversion function
   * @tparam Z - java data type for value param
   * @return Future[Result]
   */
  protected def resultMatcher[Z](v: Optional[Z], f: Optional[Z] => AnyRef, msg: Option[String]): Future[Result] = {
    Future {
      f(v)
    }.map {
      case Some(x) => Ok(s"${x}")
      // bad request
      case None => NotFound(errAsJson(NOT_FOUND, "bad_request", s"${msg.getOrElse("Could not find requested id")}"))
    }
  }


  protected def optionConverter[A](o: Optional[A]): Option[A] = if (o.isPresent) Some(o.get) else None

  /**
   * @tparam B  - could be enterprise or unit in next iteration and combine func
   */
  protected def toScalaList[B](l: Optional[java.util.List[B]]): Option[List[B]] = if (l.isPresent) Some(l.get.toList) else None

}
