package controllers.v1

import play.api.mvc.{Controller, Result}
import com.typesafe.scalalogging.StrictLogging

import scala.util.{Failure, Success, Try}
import scala.concurrent.Future
import scala.util.control.ControlThrowable

/**
 * Created by haqa on 10/07/2017.
 */
trait ControllerUtils extends Controller with StrictLogging {

  protected[this] def resultAsResponse(f: => Future[Result]): Future[Result] = Try(f) match {
    case Success(g) => g
    case Failure(err) =>
      logger.error("Unable to produce response.", err)
      Future.successful {
        InternalServerError(s"{err = '${err}'}")
      }
  }


  protected def errAsStatus (t : Throwable) = ???


  def safely[T](handler: PartialFunction[Throwable, T]): PartialFunction[Throwable, T] = {
    case ex: ControlThrowable => throw ex
    // case ex: OutOfMemoryError (Assorted other nasty exceptions you don't want to catch)

    //If it's an exception they handle, pass it on
    case ex: Throwable if handler.isDefinedAt(ex) => handler(ex)

    // If they didn't handle it, rethrow. This line isn't necessary, just for clarity
    case ex: Throwable => throw ex
  }



//  def doSomething: Unit = {
//    try {
//      somethingDangerous
//    } catch safely {
//      ex: Throwable => println("AHHH")
//    }
//  }





}
