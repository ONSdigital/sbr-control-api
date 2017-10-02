package utils

import scala.util.{ Failure, Try, Success }

import play.api.mvc.{ AnyContent, Request }

sealed case class UriBuilder(
  environment: Option[String],
  domain: Option[String],
  version: Option[String],
  port: Option[Int] = None,
  path: Option[String] = None,
  protocol: Option[String] = None
)
object UriBuilder {

  lazy val withVersion: Option[String] = ???
  lazy val withEnvironment: Option[String] = ???
  lazy val withPort: Option[Int] = ???

  def apply(request: Request[AnyContent]): UriBuilder = {
    lazy val withVersion: Option[String] = ???
    lazy val withEnvironment: Option[String] = ???
    lazy val withPort: Option[Int] = Try((request.host.split(":") takeRight 1).mkString.toInt) match {
      case Success(s) => Some(s)
      case Failure(_) => None
    }
    UriBuilder(withEnvironment, Option(request.domain), withVersion, withPort, Some(request.path))
  }

}
