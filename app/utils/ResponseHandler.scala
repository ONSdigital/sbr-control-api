package utils

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.ws.WSResponse

import scala.concurrent.Future

object ResponseHandler {
  def make[A](onSuccess: WSResponse => A)(onRecovery: PartialFunction[Throwable, A]): Future[WSResponse] => Future[A] =
    (futResponse: Future[WSResponse]) =>
      futResponse.map(onSuccess).recover {
        onRecovery
      }
}
