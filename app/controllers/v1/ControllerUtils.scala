package controllers.v1

import java.time.YearMonth
import java.time.format.{DateTimeFormatter, DateTimeParseException}

import play.api.mvc.{AnyContent, Controller, Request, Result}
import com.typesafe.scalalogging.StrictLogging

import scala.util.{Failure, Success, Try}
import scala.concurrent.Future
import uk.gov.ons.sbr.data.controller.UnitController
import uk.gov.ons.sbr.data.controller.EnterpriseController
import utils.Utilities.errAsJson

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
    val yearAndMonth = Try(YearMonth.parse(rawDate, DateTimeFormatter.ofPattern("yyyy-MM")))
    yearAndMonth match {
      case Success(s) => key -> Some(s)
      case Failure(ex: DateTimeParseException) =>
        logger.error("cannot parse date to YearMonth object", ex)
        key -> None
    }
  }


  protected def futureResult (r: Result) = Future.successful(r)

  protected def futureErr (ex: Exception) = Future.failed(ex)

  protected def futureTryRes [T](f: Try[T]) = Future.fromTry(f)


  protected def resultMatcher [T, Z] (f: Z => T, msg: Option[String]) : Future[Result] = {
    Future {
      f }.map {
      case x => Ok(x)
      case _ => BadRequest(errAsJson(BAD_REQUEST,"bad_request", s"${msg.getOrElse("Could not find requested id")}"))
    }
  }


}
