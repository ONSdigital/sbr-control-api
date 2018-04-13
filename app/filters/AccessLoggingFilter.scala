package filters

import akka.stream.Materializer
import com.typesafe.scalalogging.LazyLogging
import javax.inject.Inject
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.{ Filter, RequestHeader, Result }

import scala.concurrent.Future

/*
 * See https://www.playframework.com/documentation/2.5.x/ScalaLogging
 */
class AccessLoggingFilter @Inject() (implicit val mat: Materializer) extends Filter with LazyLogging {
  def apply(next: (RequestHeader) => Future[Result])(request: RequestHeader): Future[Result] = {
    val resultFuture = next(request)
    resultFuture.foreach(result => {
      val msg = s"method=${request.method} uri=${request.uri} remote-address=${request.remoteAddress}" +
        s" status=${result.header.status}"
      logger.info(msg)
    })

    resultFuture
  }
}
