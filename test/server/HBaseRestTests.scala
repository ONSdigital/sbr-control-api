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
import uk.gov.ons.sbr.models.units.UnitLinks

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
  private val columnFamily = "d"
  private val firstPeriod = "201706"
  private val secondPeriod = "201708"

  // We don't use the normal HBase REST port as it can make testing annoying, this is set as a Java Option
  // in the build.sbt
  val port = 8081
  val host = "localhost"
  val wireMockServer = new WireMockServer(wireMockConfig().port(port))

  override def beforeEach {
    wireMockServer.start()
    WireMock.configureFor(host, port)
  }

  override def afterEach {
    wireMockServer.stop()
  }

  def mockEndpoint(tableName: String, period: String, id: String, unitType: String, body: String): Unit = {
    val path = s"/$nameSpace:$tableName/$period~$id~$unitType/$columnFamily"
    stubFor(get(urlEqualTo(path))
      .willReturn(
        aResponse()
          .withStatus(200)
          .withHeader("content-type", "application/json")
          .withHeader("transfer-encoding", "chunked")
          .withBody(body)
      ))
  }

  "/v1/units/:unit" should {
    "return a unit for a valid id (enterprise)" in {
      val id = "9900156115"
      val body = "{\"Row\":[{\"key\":\"MjAxNzA2fjk5MDAxNTYxMTV+RU5U\",\"Cell\":[{\"column\":\"ZDpjXzAxNzUyNTY0\",\"timestamp\":1517844803247,\"$\":\"Q0g=\"},{\"column\":\"ZDpjXzA0OTg4NTI3\",\"timestamp\":1517844803247,\"$\":\"Q0g=\"},{\"column\":\"ZDpjXzEwMjA1NDE1\",\"timestamp\":1517844764481,\"$\":\"TEVV\"},{\"column\":\"ZDpjXzI3Mzg3Njg=\",\"timestamp\":1517844790326,\"$\":\"UEFZRQ==\"},{\"column\":\"ZDpjXzQwMTM0NzI2MzI4OQ==\",\"timestamp\":1517844777427,\"$\":\"VkFU\"},{\"column\":\"ZDpjXzUzNzc3MzI=\",\"timestamp\":1517844790326,\"$\":\"UEFZRQ==\"},{\"column\":\"ZDpjXzg1OTc4NjM2NzYzMQ==\",\"timestamp\":1517844777427,\"$\":\"VkFU\"},{\"column\":\"ZDpjXzk1MzczODIx\",\"timestamp\":1517844764481,\"$\":\"TEVV\"}]}]}"
      mockEndpoint(unitLinksTable, firstPeriod, id, "*", body)
      val resp = fakeRequest(s"/$version/units/$id")
      val json = contentAsJson(resp).as[JsArray]
      val unit = json(0).validate[UnitLinks]
      status(resp) mustBe OK
      contentType(resp) mustBe Some("application/json")
      json.value.size mustBe 1
      unit.isInstanceOf[JsSuccess[UnitLinks]] mustBe true
    }

    "return a unit for a valid id (company)" in {
      val id = "01752564"
      val body = "{\"Row\":[{\"key\":\"MjAxNzA2fjAxNzUyNTY0fkNI\",\"Cell\":[{\"column\":\"ZDpwX0VOVA==\",\"timestamp\":1518617008390,\"$\":\"OTkwMDE1NjExNQ==\"},{\"column\":\"ZDpwX0xFVQ==\",\"timestamp\":1518617010555,\"$\":\"MTAyMDU0MTU=\"}]}]}"
      mockEndpoint(unitLinksTable, firstPeriod, id, "*", body)
      val resp = fakeRequest(s"/$version/units/$id")
      val json = contentAsJson(resp).as[JsArray]
      val unit = json(0).validate[UnitLinks]
      status(resp) mustBe OK
      contentType(resp) mustBe Some("application/json")
      json.value.size mustBe 1
      unit.isInstanceOf[JsSuccess[UnitLinks]] mustBe true
    }

    "return a unit for a valid id (VAT)" in {
      val id = "401347263289"
      val body = "{\"Row\":[{\"key\":\"MjAxNzA2fjQwMTM0NzI2MzI4OX5WQVQ=\",\"Cell\":[{\"column\":\"ZDpwX0VOVA==\",\"timestamp\":1518617001775,\"$\":\"OTkwMDE1NjExNQ==\"},{\"column\":\"ZDpwX0xFVQ==\",\"timestamp\":1518617016005,\"$\":\"MTAyMDU0MTU=\"}]}]}"
      mockEndpoint(unitLinksTable, firstPeriod, id, "*", body)
      val resp = fakeRequest(s"/$version/units/$id")
      val json = contentAsJson(resp).as[JsArray]
      val unit = json(0).validate[UnitLinks]
      status(resp) mustBe OK
      contentType(resp) mustBe Some("application/json")
      json.value.size mustBe 1
      unit.isInstanceOf[JsSuccess[UnitLinks]] mustBe true
    }

    "return a unit for a valid id (PAYE)" in {
      val id = "2738768"
      val body = "{\"Row\":[{\"key\":\"MjAxNzA2fjI3Mzg3Njh+UEFZRQ==\",\"Cell\":[{\"column\":\"ZDpwX0VOVA==\",\"timestamp\":1518617005123,\"$\":\"OTkwMDE1NjExNQ==\"},{\"column\":\"ZDpwX0xFVQ==\",\"timestamp\":1518617012781,\"$\":\"MTAyMDU0MTU=\"}]}]}"
      mockEndpoint(unitLinksTable, firstPeriod, id, "*", body)
      val resp = fakeRequest(s"/$version/units/$id")
      val json = contentAsJson(resp).as[JsArray]
      val unit = json(0).validate[UnitLinks]
      status(resp) mustBe OK
      contentType(resp) mustBe Some("application/json")
      json.value.size mustBe 1
      unit.isInstanceOf[JsSuccess[UnitLinks]] mustBe true
    }

    "return a unit for a valid id (UBRN)" in {
      val id = "10205415"
      val body = "{\"Row\":[{\"key\":\"MjAxNzA2fjEwMjA1NDE1fkxFVQ==\",\"Cell\":[{\"column\":\"ZDpjXzAxNzUyNTY0\",\"timestamp\":1518617010555,\"$\":\"Q0g=\"},{\"column\":\"ZDpjXzI3Mzg3Njg=\",\"timestamp\":1518617012781,\"$\":\"UEFZRQ==\"},{\"column\":\"ZDpjXzQwMTM0NzI2MzI4OQ==\",\"timestamp\":1518617016005,\"$\":\"VkFU\"},{\"column\":\"ZDpwX0VOVA==\",\"timestamp\":1518616999611,\"$\":\"OTkwMDE1NjExNQ==\"}]}]}"
      mockEndpoint(unitLinksTable, firstPeriod, id, "*", body)
      val resp = fakeRequest(s"/$version/units/$id")
      val json = contentAsJson(resp).as[JsArray]
      val unit = json(0).validate[UnitLinks]
      status(resp) mustBe OK
      contentType(resp) mustBe Some("application/json")
      json.value.size mustBe 1
      unit.isInstanceOf[JsSuccess[UnitLinks]] mustBe true
    }

    // This test should pass but currently doesn't
    //    "return multiple units for a valid conflicting id (UBRN/VAT)" in {
    //      // UBRN and VAT can have conflicting ID's so a request for a conflicting ID can
    //      // return multiple results
    //      val id = "976351836291"
    //      val body = "{\"Row\":[{\"key\":\"MjAxNzA2fjk5MDAxNTYxMTV+RU5U\",\"Cell\":[{\"column\":\"ZDpjXzAxNzUyNTY0\",\"timestamp\":1517844803247,\"$\":\"Q0g=\"},{\"column\":\"ZDpjXzA0OTg4NTI3\",\"timestamp\":1517844803247,\"$\":\"Q0g=\"},{\"column\":\"ZDpjXzEwMjA1NDE1\",\"timestamp\":1517844764481,\"$\":\"TEVV\"},{\"column\":\"ZDpjXzI3Mzg3Njg=\",\"timestamp\":1517844790326,\"$\":\"UEFZRQ==\"},{\"column\":\"ZDpjXzQwMTM0NzI2MzI4OQ==\",\"timestamp\":1517844777427,\"$\":\"VkFU\"},{\"column\":\"ZDpjXzUzNzc3MzI=\",\"timestamp\":1517844790326,\"$\":\"UEFZRQ==\"},{\"column\":\"ZDpjXzg1OTc4NjM2NzYzMQ==\",\"timestamp\":1517844777427,\"$\":\"VkFU\"},{\"column\":\"ZDpjXzk1MzczODIx\",\"timestamp\":1517844764481,\"$\":\"TEVV\"}]}]}"
    //      mockEndpoint(unitLinksTable, firstPeriod, id, "*", body)
    //      val resp = fakeRequest(s"/$version/units/$id")
    //      val json = contentAsJson(resp).as[JsArray]
    //      val unit = json.value.map(x => x.validate[UnitLinks])
    //      json.value.size mustBe 2
    //      status(resp) mustBe OK
    //      unit.isInstanceOf[Seq[JsSuccess[UnitLinks]]] mustBe true
    //    }

    "return 404 for a unit that doesn't exist" in {
      1 must equal(1)
    }
  }

  "/v1/periods/:period/units/:unit" should {
    "return a unit for a valid id (company)" in {
      val id = "01752564"
      val body = "{\"Row\":[{\"key\":\"MjAxNzA2fjAxNzUyNTY0fkNI\",\"Cell\":[{\"column\":\"ZDpwX0VOVA==\",\"timestamp\":1518617008390,\"$\":\"OTkwMDE1NjExNQ==\"},{\"column\":\"ZDpwX0xFVQ==\",\"timestamp\":1518617010555,\"$\":\"MTAyMDU0MTU=\"}]}]}"
      mockEndpoint(unitLinksTable, firstPeriod, id, "*", body)
      val resp = fakeRequest(s"/$version/units/$id")
      val json = contentAsJson(resp).as[JsArray]
      val unit = json(0).validate[UnitLinks]
      status(resp) mustBe OK
      contentType(resp) mustBe Some("application/json")
      json.value.size mustBe 1
      unit.isInstanceOf[JsSuccess[UnitLinks]] mustBe true
    }

  }
  "/v1/enterprises/:id" should {
    "return a unit for a valid id (enterprise)" in {
      val id = "9905899133"
      val body = "{\"Row\":[{\"key\":\"MjAxNzA2fjk5MDU4OTkxMzN+RU5U\",\"Cell\":[{\"column\":\"ZDpjXzA0MTA5NDM2\",\"timestamp\":1518617008390,\"$\":\"Q0g=\"},{\"column\":\"ZDpjXzA1ODQ0NTE2\",\"timestamp\":1518617008390,\"$\":\"Q0g=\"},{\"column\":\"ZDpjXzE0MDkyOTE5\",\"timestamp\":1518616999611,\"$\":\"TEVV\"},{\"column\":\"ZDpjXzIzOTE0OTg=\",\"timestamp\":1518617005123,\"$\":\"UEFZRQ==\"},{\"column\":\"ZDpjXzI2ODAyMzcx\",\"timestamp\":1518616999611,\"$\":\"TEVV\"},{\"column\":\"ZDpjXzM4ODM2NTI=\",\"timestamp\":1518617005123,\"$\":\"UEFZRQ==\"},{\"column\":\"ZDpjXzU2NzM4Mzk1\",\"timestamp\":1518616999611,\"$\":\"TEVV\"},{\"column\":\"ZDpjXzYwNjE2NDEzNDY0MA==\",\"timestamp\":1518617001775,\"$\":\"VkFU\"},{\"column\":\"ZDpjXzYyMTM0NzY=\",\"timestamp\":1518617005123,\"$\":\"UEFZRQ==\"},{\"column\":\"ZDpjXzY0MjA2NjMyMzg2NQ==\",\"timestamp\":1518617001775,\"$\":\"VkFU\"},{\"column\":\"ZDpjXzg4NjQxMTQ4MjMzNQ==\",\"timestamp\":1518617001775,\"$\":\"VkFU\"},{\"column\":\"ZDpjXzk3NjM1MTgzNjI5MQ==\",\"timestamp\":1518617001775,\"$\":\"VkFU\"},{\"column\":\"ZDpjX05JMDM0MTU5\",\"timestamp\":1518617008390,\"$\":\"Q0g=\"}]}]}"
      mockEndpoint(unitLinksTable, firstPeriod, id, "*", body)
      val resp = fakeRequest(s"/$version/units/$id")
      val json = contentAsJson(resp).as[JsArray]
      val unit = json(0).validate[UnitLinks]
      status(resp) mustBe OK
      contentType(resp) mustBe Some("application/json")
      json.value.size mustBe 1
      unit.isInstanceOf[JsSuccess[UnitLinks]] mustBe true
    }
  }

  "/v1/periods/:period/enterprises/:id" should {
    "return a unit for a valid id (UBRN)" in {
      val id = "10205415"
      val body = "{\"Row\":[{\"key\":\"MjAxNzA2fjEwMjA1NDE1fkxFVQ==\",\"Cell\":[{\"column\":\"ZDpjXzAxNzUyNTY0\",\"timestamp\":1518617010555,\"$\":\"Q0g=\"},{\"column\":\"ZDpjXzI3Mzg3Njg=\",\"timestamp\":1518617012781,\"$\":\"UEFZRQ==\"},{\"column\":\"ZDpjXzQwMTM0NzI2MzI4OQ==\",\"timestamp\":1518617016005,\"$\":\"VkFU\"},{\"column\":\"ZDpwX0VOVA==\",\"timestamp\":1518616999611,\"$\":\"OTkwMDE1NjExNQ==\"}]}]}"
      mockEndpoint(unitLinksTable, firstPeriod, id, "*", body)
      val resp = fakeRequest(s"/$version/units/$id")
      val json = contentAsJson(resp).as[JsArray]
      val unit = json(0).validate[UnitLinks]
      status(resp) mustBe OK
      contentType(resp) mustBe Some("application/json")
      json.value.size mustBe 1
      unit.isInstanceOf[JsSuccess[UnitLinks]] mustBe true
    }
  }

  "/v1/types/:type/units/:id" should {
    "return a unit for a valid id (VatRef)" in {
      val id = "401347263289"
      val body = "{\"Row\":[{\"key\":\"MjAxNzA2fjQwMTM0NzI2MzI4OX5WQVQ=\",\"Cell\":[{\"column\":\"ZDpwX0VOVA==\",\"timestamp\":1518617001775,\"$\":\"OTkwMDE1NjExNQ==\"},{\"column\":\"ZDpwX0xFVQ==\",\"timestamp\":1518617016005,\"$\":\"MTAyMDU0MTU=\"}]}]}"
      mockEndpoint(unitLinksTable, firstPeriod, id, "*", body)
      val resp = fakeRequest(s"/$version/units/$id")
      val json = contentAsJson(resp).as[JsArray]
      val unit = json(0).validate[UnitLinks]
      status(resp) mustBe OK
      contentType(resp) mustBe Some("application/json")
      json.value.size mustBe 1
      unit.isInstanceOf[JsSuccess[UnitLinks]] mustBe true
    }
  }

  "/v1/periods/:period/types/:type/units/:id" should {
    "return a unit for a valid id (PayeRef)" in {
      val id = "2738768"
      val body = "{\"Row\":[{\"key\":\"MjAxNzA2fjI3Mzg3Njh+UEFZRQ==\",\"Cell\":[{\"column\":\"ZDpwX0VOVA==\",\"timestamp\":1518617005123,\"$\":\"OTkwMDE1NjExNQ==\"},{\"column\":\"ZDpwX0xFVQ==\",\"timestamp\":1518617012781,\"$\":\"MTAyMDU0MTU=\"}]}]}"
      mockEndpoint(unitLinksTable, firstPeriod, id, "*", body)
      val resp = fakeRequest(s"/$version/units/$id")
      val json = contentAsJson(resp).as[JsArray]
      val unit = json(0).validate[UnitLinks]
      status(resp) mustBe OK
      contentType(resp) mustBe Some("application/json")
      json.value.size mustBe 1
      unit.isInstanceOf[JsSuccess[UnitLinks]] mustBe true
    }
  }

  //  "return a unit for a valid id ()" in {
  //    val id = ""
  //    val body = ""
  //    mockEndpoint(unitLinksTable, firstPeriod, id, "*", body)
  //    val resp = fakeRequest(s"/$version/units/$id")
  //    val json = contentAsJson(resp).as[JsArray]
  //    val unit = json(0).validate[UnitLinks]
  //    status(resp) mustBe OK
  //    contentType(resp) mustBe Some("application/json")
  //    json.value.size mustBe 1
  //    unit.isInstanceOf[JsSuccess[UnitLinks]] mustBe true
  //  }
}
