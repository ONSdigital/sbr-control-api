package server

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.Helpers._

import resource.TestUtils

/**
 * Created by haqa on 28/09/2017.
 */
class HBaseConnectTests extends TestUtils with GuiceOneAppPerSuite {

  private val enterpriseId = "9900156115"
  private val badId = "99"
  private val entType = "ENT"
  private val invalidType = "UNKNOWN"
  private val badDate = "2006"
  private val period = "201706"
  private val wrongPeriod = "200006"
  private val expectedChild = "100002323948"

  "Unit Search on SQLConnect should" should {
    "returns a unit for a given id" ignore {
      val search = fakeRequest(s"/v1/units/$enterpriseId")
      status(search) mustBe OK
      contentType(search) mustBe Some("application/json")
      val json = contentAsJson(search)
      (json.head \ "id").as[String] must equal(enterpriseId)
      (json.head \ "children" \ expectedChild).as[String] must equal("LEU")
    }

    "returns BadRequest with short id" in {
      val search = fakeRequest(s"/v1/units/$badId")
      status(search) mustBe BAD_REQUEST
      contentType(search) mustBe Some("application/json")
      val json = contentAsJson(search)
      (json \ "status").as[Int] must equal(BAD_REQUEST)
      contentAsString(search) must include("invalid_key")
    }

    "returns BadRequest due to bad period" in {
      val search = fakeRequest(s"/v1/periods/$badDate/units/$enterpriseId")
      status(search) mustBe BAD_REQUEST
      contentType(search) mustBe Some("application/json")
      val json = contentAsJson(search)
      (json \ "status").as[Int] must equal(BAD_REQUEST)
      (json \ "message_en").as[String] must include("exception java.time.format.DateTimeParseException:")
    }
  }

  "Search by unit type param on SQLConnect" should {
    "return a unit with corresponding id and unit type" ignore {
      val search = fakeRequest(s"/v1/types/$entType/units/$enterpriseId")
      status(search) mustBe OK
      contentType(search) mustBe Some("application/json")
      val json = contentAsJson(search)
      (json \ "id").as[String] must equal(enterpriseId)
      // ent will have leu children
      (json \ "children" \ expectedChild).as[String] must equal("LEU")
    }

    "returns an invalid type warning for unrecognised type param" ignore {
      val search = fakeRequest(s"/v1/types/$invalidType/units/$enterpriseId")
      status(search) mustBe NOT_FOUND
      contentType(search) mustBe Some("application/json")
      contentAsString(search) must include(s"Could not find unit with id $enterpriseId and category $invalidType")
    }
  }

  "Search for enterprises on SQLConnect" should {
    "return enterprise with matching id and period" ignore {
      val search = fakeRequest(s"/v1/periods/$period/enterprises/$enterpriseId")
      status(search) mustBe OK
      contentType(search) mustBe Some("application/json")
      val json = contentAsJson(search)
      println(json)
      (json \ "id").as[Long] must equal(enterpriseId.toLong)
      (json \ "period").as[String] must equal(period)
    }

    "return BadRequest with invalid enterprise id" ignore {
      val search = fakeRequest(s"/v1/periods/$wrongPeriod/enterprises/$enterpriseId")
      status(search) mustBe NOT_FOUND
      contentType(search) mustBe Some("application/json")
      val json = contentAsJson(search)
      (json \ "status").as[Int] must equal(NOT_FOUND)
      (json \ "message_en").as[String] must include("2000-06")

    }
  }

}
