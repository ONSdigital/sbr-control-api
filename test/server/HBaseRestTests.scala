package server

import play.api.test.Helpers._
import resource.TestUtils
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import play.api.libs.json.{ JsArray, JsSuccess }
import uk.gov.ons.sbr.models.units.{ EnterpriseUnit, UnitLinks }

/**
 * Created by coolit on 13/02/2018.
 */
class HBaseRestTests extends TestUtils with BeforeAndAfterEach with GuiceOneAppPerSuite {

  // TODO:
  // - test each type of endpoint
  // - test with auth details (this requires small modifications to the configuration)
  // - we could read in the test data and base64 encode it etc. rather than just returning the data string

  private val version = "v1"
  private val nameSpace = "sbr_local_db"
  private val unitLinksTable = "unit_links"
  private val enterpriseTable = "enterprise"
  private val columnFamily = "d"
  private val firstPeriod = "201706"
  private val secondPeriod = "201708"

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

  def mockEndpoint(tableName: String, period: String, id: String, unitType: Option[String], body: String): Unit = {
    val path = unitType match {
      case Some(s) => s"/$nameSpace:$tableName/$period~$id~$s/$columnFamily"
      case None => s"/$nameSpace:$tableName/${id.reverse}~$period/$columnFamily"
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

  "/v1/periods/:period/enterprises/:id" should {
    "return an enterprise for a valid enterprise id" in {
      val id = "9900156115"
      val body = "{\"Row\":[{\"key\":\"MjAxNzA2fjk5MDAxNTYxMTU=\",\"Cell\":[{\"column\":\"ZDpOdW1fVW5pcXVlX1BheWVSZWZz\",\"timestamp\":1518607261140,\"$\":\"MQ==\"},{\"column\":\"ZDpOdW1fVW5pcXVlX1ZhdFJlZnM=\",\"timestamp\":1518607261140,\"$\":\"MQ==\"},{\"column\":\"ZDpQQVlFX2pvYnM=\",\"timestamp\":1518607261140,\"$\":\"cGF5ZWpvYnM=\"},{\"column\":\"ZDplbXBsb3llZXM=\",\"timestamp\":1518607261140,\"$\":\"MTAwMA==\"},{\"column\":\"ZDplbnRfYWRkcmVzczE=\",\"timestamp\":1518607261140,\"$\":\"NTEzIEhpZ2ggU3RyZWV0\"},{\"column\":\"ZDplbnRfYWRkcmVzczI=\",\"timestamp\":1518607261140,\"$\":\"TGl0dGxlIFdvcnNsZXk=\"},{\"column\":\"ZDplbnRfYWRkcmVzczM=\",\"timestamp\":1518607261140,\"$\":\"QnVyeQ==\"},{\"column\":\"ZDplbnRfYWRkcmVzczQ=\",\"timestamp\":1518607261140,\"$\":\"TWFuY2hlc3Rlcg==\"},{\"column\":\"ZDplbnRfYWRkcmVzczU=\",\"timestamp\":1518607261140,\"$\":\"R3RyIE1hbmNoZXN0ZXI=\"},{\"column\":\"ZDplbnRfbmFtZQ==\",\"timestamp\":1518607261140,\"$\":\"VEVTQ08=\"},{\"column\":\"ZDplbnRfcG9zdGNvZGU=\",\"timestamp\":1518607261140,\"$\":\"T0sxNiA1WFE=\"},{\"column\":\"ZDplbnRyZWY=\",\"timestamp\":1518607261140,\"$\":\"OTkwMDE1NjExNQ==\"},{\"column\":\"ZDpsZWdhbHN0YXR1cw==\",\"timestamp\":1518607261140,\"$\":\"Mg==\"},{\"column\":\"ZDpzdGFuZGFyZF92YXRfdHVybm92ZXI=\",\"timestamp\":1518607261140,\"$\":\"MQ==\"},{\"column\":\"ZDp1cGRhdGVkQnk=\",\"timestamp\":1518607261140,\"$\":\"RGF0YSBMb2Fk\"}]}]}"
      mockEndpoint(enterpriseTable, firstPeriod, id, None, body)
      val resp = fakeRequest(s"/$version/periods/$firstPeriod/enterprises/$id")
      val json = contentAsJson(resp)
      val ent = json.validate[EnterpriseUnit]
      status(resp) mustBe OK
      contentType(resp) mustBe Some("application/json")
      ent.isInstanceOf[JsSuccess[EnterpriseUnit]] mustBe true
    }
  }

  "/v1/periods/:period/units/:unit" should {
    "return a unit for a valid id (enterprise)" in {
      val id = "9900156115"
      val body = "{\"Row\":[{\"key\":\"MjAxNzA2fjAxNzUyNTY0fkNI\",\"Cell\":[{\"column\":\"ZDpwX0VOVA==\",\"timestamp\":1518617008390,\"$\":\"OTkwMDE1NjExNQ==\"},{\"column\":\"ZDpwX0xFVQ==\",\"timestamp\":1518617010555,\"$\":\"MTAyMDU0MTU=\"}]}]}"
      mockEndpoint(unitLinksTable, firstPeriod, id, Some("*"), body)
      val resp = fakeRequest(s"/$version/periods/$firstPeriod/units/$id")
      val json = contentAsJson(resp).as[JsArray]
      val unit = json(0).validate[UnitLinks]
      status(resp) mustBe OK
      contentType(resp) mustBe Some("application/json")
      json.value.size mustBe 1
      unit.isInstanceOf[JsSuccess[UnitLinks]] mustBe true
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
  }

  "/v1/periods/:period/types/:type/units/:id" should {
    "return a unit for a valid id (PAYE)" in {
      val id = "2738768"
      val unitType = "PAYE"
      val body = "{\"Row\":[{\"key\":\"MjAxNzA2fjI3Mzg3Njh+UEFZRQ==\",\"Cell\":[{\"column\":\"ZDpwX0VOVA==\",\"timestamp\":1517844790326,\"$\":\"OTkwMDE1NjExNQ==\"},{\"column\":\"ZDpwX0xFVQ==\",\"timestamp\":1517844827832,\"$\":\"MTAyMDU0MTU=\"}]}]}"
      mockEndpoint(unitLinksTable, firstPeriod, id, Some(unitType), body)
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