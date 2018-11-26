package scala.server

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.mvc.Result
import play.api.test.Helpers._
import play.api.test._

import scala.concurrent.Future

/**
 * Test application routes operate
 */
class RouteSpec extends PlaySpec with GuiceOneAppPerSuite {

  "No Route" should {
    "send 404 on a bad request" in {
      route(app, FakeRequest(GET, "/boum")).map(status) mustBe Some(NOT_FOUND)
    }
  }

  "HomeController" should {
    "render default app route" in {
      val home = fakeRequest("/")
      // redirect
      status(home) mustEqual SEE_OTHER
      val res = getValue(redirectLocation(home))
      res must include("/health")
      flash(home).get("status") mustBe Some("ok")
    }

    "display swagger documentation" in {
      val docs = fakeRequest("/docs")
      status(docs) mustEqual SEE_OTHER
      val res = getValue(redirectLocation(docs))
      res must include("/swagger-ui/index.html")
      contentAsString(docs) mustNot include("Not_FOUND")
    }
  }

  "HealthController" should {
    "display short health report as json" in {
      val health = fakeRequest("/health")
      status(health) mustEqual OK
      contentType(health) mustBe Some("application/json")
      contentAsString(health).toLowerCase must include(s""""status": "ok"""")
    }
  }

  private def fakeRequest(url: String, method: String = GET, appInstance: Application = app): Future[Result] =
    route(appInstance, FakeRequest(method, url)).getOrElse(sys.error(s"Route $url does not exist"))

  private def getValue(json: Option[String]): String =
    json match {
      case Some(x: String) => s"$x"
      case _ => sys.error("No Value failed. Forcing test failure")
    }
}
