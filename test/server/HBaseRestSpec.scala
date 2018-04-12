package server

import play.api.test.Helpers._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import play.api.libs.json.{ JsArray, JsSuccess }
import support.TestUtils
import uk.gov.ons.sbr.models.units.{ EnterpriseUnit, UnitLinks }

class HBaseRestSpec extends TestUtils with BeforeAndAfterEach with GuiceOneAppPerSuite {

  // TODO:
  // - test each type of endpoint
  // - test with auth details (this requires small modifications to the configuration)
  // - we could read in the test data and base64 encode it etc. rather than just returning the data string

  private val version = "v1"
  private val nameSpace = "sbr_control_db"
  private val unitLinksTable = "unit_links_test"
  private val enterpriseTable = "enterprise"
  private val columnFamilyEnterprise = "d"
  private val columnFamilyLinks = "l"
  private val firstPeriod = "201706"

  // We don't use the normal HBase REST port as it can make testing annoying, this is set as a Java Option
  // in the build.sbt
  val port = 8075
  val host = "localhost"
  val wireMockServer = new WireMockServer(wireMockConfig().port(port))

  override def beforeEach {
    wireMockServer.start()
    WireMock.configureFor(host, port)
  }

  override def afterEach {
    wireMockServer.stop()
  }

  def mockEndpoint(tableName: String, period: Option[String], id: String, unitType: Option[String], body: String): Unit = {
    val path = unitType match {
      case Some(s) => period match {
        case Some(p) => s"/$nameSpace:$tableName/$id~$s~$p/$columnFamilyLinks"
        case None => s"/$nameSpace:$tableName/$id~$s/$columnFamilyLinks"
      }
      case None => period match {
        case Some(p) => s"/$nameSpace:$tableName/${id.reverse}~$p/$columnFamilyEnterprise"
        case None => s"/$nameSpace:$tableName/${id.reverse}~*/$columnFamilyEnterprise"
      }
    }
    stubFor(get(urlEqualTo(path))
      .willReturn(
        aResponse()
          .withStatus(200)
          .withHeader("content-type", "application/json")
          .withHeader("transfer-encoding", "chunked")
          .withBody(body)
      ))
  }

  "/v1/enterprises/:id" should {
    "return an enterprise for a valid enterprise id" in {
      val id = "12345"
      val body = "{\"Row\":[{\"key\":\"NTQzMjF+MjAxNzEy\",\"Cell\":[{\"column\":\"ZDplbnRfbmFtZQ==\",\"timestamp\":1519809888703,\"$\":\"VGVzY28=\"},{\"column\":\"ZDplbnRyZWY=\",\"timestamp\":1519809884065,\"$\":\"MTIzNDU=\"}]},{\"key\":\"NTQzMjF+MjAxODAx\",\"Cell\":[{\"column\":\"ZDplbnRfbmFtZQ==\",\"timestamp\":1519809879783,\"$\":\"VGVzY28=\"},{\"column\":\"ZDplbnRyZWY=\",\"timestamp\":1519809874856,\"$\":\"MTIzNDU=\"}]},{\"key\":\"NTQzMjF+MjAxODAy\",\"Cell\":[{\"column\":\"ZDplbnRfbmFtZQ==\",\"timestamp\":1519809867579,\"$\":\"VGVzY28=\"},{\"column\":\"ZDplbnRyZWY=\",\"timestamp\":1519809865127,\"$\":\"MTIzNDU=\"}]}]}"
      mockEndpoint(enterpriseTable, None, id, None, body)
      val resp = fakeRequest(s"/$version/enterprises/$id")
      val json = contentAsJson(resp)
      val ent = json.validate[EnterpriseUnit]
      status(resp) mustBe OK
      contentType(resp) mustBe Some("application/json")
      ent.isInstanceOf[JsSuccess[EnterpriseUnit]] mustBe true
    }
  }

  //  "/v1/periods/:period/enterprises/:id" should {
  //    "return an enterprise for a valid enterprise id" in {
  //      val id = "12345"
  //      val body = "{\"Row\":[{\"key\":\"NTQzMjF+MjAxODAy\",\"Cell\":[{\"column\":\"ZDplbnRfbmFtZQ==\",\"timestamp\":1519809867579,\"$\":\"VGVzY28=\"},{\"column\":\"ZDplbnRyZWY=\",\"timestamp\":1519809865127,\"$\":\"MTIzNDU=\"}]}]}"
  //      mockEndpoint(enterpriseTable, Some(firstPeriod), id, None, body)
  //      val resp = fakeRequest(s"/$version/periods/$firstPeriod/enterprises/$id")
  //      val json = contentAsJson(resp)
  //      val ent = json.validate[EnterpriseUnit]
  //      status(resp) mustBe OK
  //      contentType(resp) mustBe Some("application/json")
  //      ent.isInstanceOf[JsSuccess[EnterpriseUnit]] mustBe true
  //    }
  //  }

  "/v1/units/:unit" should {
    "return a unit for a valid id (enterprise)" in {
      val id = "12345"
      val body = "{\"Row\":[{\"key\":\"MTIzNDV+RU5UfjIwMTgwMQ==\",\"Cell\":[{\"column\":\"bDpjXzE5MjgzNzQ2NTk5OQ==\",\"timestamp\":1519823610846,\"$\":\"TEVV\"},{\"column\":\"bDpjXzIzODQ3NTYz\",\"timestamp\":1519823616175,\"$\":\"Q0g=\"},{\"column\":\"bDpjXzM4NTc2Mzk1\",\"timestamp\":1519823621856,\"$\":\"UEFZRQ==\"},{\"column\":\"bDpjXzQxMDM3NDky\",\"timestamp\":1519823627169,\"$\":\"VkFU\"}]},{\"key\":\"MTIzNDV+RU5UfjIwMTgwMg==\",\"Cell\":[{\"column\":\"bDpjXzE5MjgzNzQ2NTk5OQ==\",\"timestamp\":1519823591909,\"$\":\"TEVV\"},{\"column\":\"bDpjXzIzODQ3NTYz\",\"timestamp\":1519823596475,\"$\":\"Q0g=\"},{\"column\":\"bDpjXzM4NTc2Mzk1\",\"timestamp\":1519823601150,\"$\":\"UEFZRQ==\"},{\"column\":\"bDpjXzQxMDM3NDky\",\"timestamp\":1519823605519,\"$\":\"VkFU\"}]}]}"
      mockEndpoint(unitLinksTable, None, id, Some("*"), body)
      val resp = fakeRequest(s"/$version/units/$id")
      val json = contentAsJson(resp).as[JsArray]
      val unit = json(0).validate[UnitLinks]
      status(resp) mustBe OK
      contentType(resp) mustBe Some("application/json")
      json.value.size mustBe 1
      unit.isInstanceOf[JsSuccess[UnitLinks]] mustBe true
    }
  }

  //    "return multiple units for a valid conflicting id (UBRN/VAT)" in {
  //      // UBRN and VAT can have conflicting ID's so a request for a conflicting ID can
  //      // return multiple results
  //      val id = "976351836291"
  //      val body = "{\"Row\":[{\"key\":\"MjAxNzA2fjk5MDAxNTYxMTV+RU5U\",\"Cell\":[{\"column\":\"ZDpjXzAxNzUyNTY0\",\"timestamp\":1517844803247,\"$\":\"Q0g=\"},{\"column\":\"ZDpjXzA0OTg4NTI3\",\"timestamp\":1517844803247,\"$\":\"Q0g=\"},{\"column\":\"ZDpjXzEwMjA1NDE1\",\"timestamp\":1517844764481,\"$\":\"TEVV\"},{\"column\":\"ZDpjXzI3Mzg3Njg=\",\"timestamp\":1517844790326,\"$\":\"UEFZRQ==\"},{\"column\":\"ZDpjXzQwMTM0NzI2MzI4OQ==\",\"timestamp\":1517844777427,\"$\":\"VkFU\"},{\"column\":\"ZDpjXzUzNzc3MzI=\",\"timestamp\":1517844790326,\"$\":\"UEFZRQ==\"},{\"column\":\"ZDpjXzg1OTc4NjM2NzYzMQ==\",\"timestamp\":1517844777427,\"$\":\"VkFU\"},{\"column\":\"ZDpjXzk1MzczODIx\",\"timestamp\":1517844764481,\"$\":\"TEVV\"}]}]}"
  //      mockEndpoint(unitLinksTable, firstPeriod, id, Some("*"), body)
  //      val resp = fakeRequest(s"/$version/periods/$firstPeriod/units/$id")
  //      val json = contentAsJson(resp).as[JsArray]
  //      val unit = json.value.map(x => x.validate[UnitLinks])
  //      json.value.size mustBe 2
  //      status(resp) mustBe OK
  //      unit.isInstanceOf[Seq[JsSuccess[UnitLinks]]] mustBe true
  //    }
  //

  "/v1/periods/:period/types/:type/units/:id" should {
    "return a unit for a valid id (PAYE)" in {
      val id = "12345"
      val unitType = "ENT"
      val body = "{\"Row\":[{\"key\":\"MTIzNDV+RU5UfjIwMTgwMg==\",\"Cell\":[{\"column\":\"bDpjXzE5MjgzNzQ2NTk5OQ==\",\"timestamp\":1519823591909,\"$\":\"TEVV\"},{\"column\":\"bDpjXzIzODQ3NTYz\",\"timestamp\":1519823596475,\"$\":\"Q0g=\"},{\"column\":\"bDpjXzM4NTc2Mzk1\",\"timestamp\":1519823601150,\"$\":\"UEFZRQ==\"},{\"column\":\"bDpjXzQxMDM3NDky\",\"timestamp\":1519823605519,\"$\":\"VkFU\"}]}]}"
      mockEndpoint(unitLinksTable, Some(firstPeriod), id, Some(unitType), body)
      val resp = fakeRequest(s"/$version/periods/$firstPeriod/types/$unitType/units/$id")
      // We don't need to convert the JSON response to an array as only a single JSON object is returned
      // as by specifying the unit type there can not be multiple responses for one id.
      val json = contentAsJson(resp)
      val unit = json.validate[UnitLinks]
      status(resp) mustBe OK
      contentType(resp) mustBe Some("application/json")
      unit.isInstanceOf[JsSuccess[UnitLinks]] mustBe true
    }
  }
}