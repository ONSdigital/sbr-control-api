package utils

import play.api.libs.ws.WSResponse

import scala.concurrent.{ExecutionContext, Future}

object ResponseHandler {
  def make[A](onSuccess: WSResponse => A)(onRecovery: PartialFunction[Throwable, A])(implicit ec: ExecutionContext): Future[WSResponse] => Future[A] =
    (futResponse: Future[WSResponse]) =>
      futResponse.map(onSuccess).recover {
        onRecovery
      }
}
