package controllers.v1

import play.api.mvc.{ AnyContent, Controller, Request, Result }
import com.typesafe.scalalogging.StrictLogging

import scala.util.{ Failure, Success, Try }
import scala.concurrent.Future

import uk.gov.ons.sbr.data.controller.UnitController
import uk.gov.ons.sbr.data.controller.EnterpriseController

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

}
