package scala.server

import play.api.test.Helpers._
import play.api.test._
import support.TestUtils

/**
 * Test application routes operate
 */
class RouteSpec extends TestUtils {

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

  "SearchController" should {
    "return 400 due to Invalid Key" in {
      val search = fakeRequest("/v1/periods/201706/enterprises/1")
      status(search) mustBe BAD_REQUEST
    }

    "return 400 with invalid date is not parsable" in {
      val dateSearch = fakeRequest("/v1/periods/201777/enterprises/1244")
      status(dateSearch) mustBe BAD_REQUEST
    }

    "return 400 short key length when searching with UnitType" in {
      val search = fakeRequest("/v1/periods/201706/types/ENT/units/1")
      status(search) mustBe BAD_REQUEST
    }

    "return 400 with invalid date when search with date, id and UnitType" in {
      val dateSearch = fakeRequest("/v1/periods/20177/types/ENT/units/124")
      status(dateSearch) mustBe BAD_REQUEST
    }
  }

  "HealthController" should {
    "display short health report as json" in {
      val health = fakeRequest("/health")
      status(health) mustEqual OK
      contentType(health) mustBe Some("application/json")
      contentAsString(health).toLowerCase must include("status: ok")
    }
  }

  "LastUpdateController" should {
    "display last modification listing" ignore {
      val last = fakeRequest("/latest", GET)
      status(last) mustBe NOT_FOUND
      contentType(last) mustBe Some("application/json")
    }
  }
}
