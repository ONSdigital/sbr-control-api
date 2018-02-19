package server

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.typesafe.config.ConfigFactory
import play.api.Configuration
import play.api.libs.ws.ahc.AhcWSClient
import play.api.test.FakeRequest
import play.api.test.Helpers._
import resource.TestUtils
import services.HBaseRestDataAccess
import org.scalatest.{BeforeAndAfterEach, FlatSpec, Matchers}
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import java.util.concurrent.TimeUnit
import javax.inject.Inject

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import org.scalatest.{BeforeAndAfterEach, FlatSpec, Matchers}
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import config.HBaseDataLoadConfig
import org.apache.hadoop.util.ToolRunner
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.ws.WSClient
import uk.gov.ons.sbr.data.hbase.HBaseConnector
import uk.gov.ons.sbr.data.hbase.load.BulkLoader

/**
 * Created by coolit on 13/02/2018.
 */
class HBaseRestTests extends TestUtils with HBaseDataLoadConfig with BeforeAndAfterEach with GuiceOneAppPerSuite { // FlatSpec with Matchers with BeforeAndAfterEach with HBaseDataLoadConfig {

  // TODO:
  // - test each type of endpoint
  // - test with auth details
  // - better modularisation of data load

  val Port = 8080
  val Host = "localhost"
  val wireMockServer = new WireMockServer(wireMockConfig().port(Port))

  override def beforeEach {
    wireMockServer.start()
    WireMock.configureFor(Host, Port)
  }

  override def afterEach {
    wireMockServer.stop()
  }

  //  implicit val system = ActorSystem()
  //  implicit val materializer = ActorMaterializer()
  //  val configuration = Configuration(ConfigFactory.load("application.conf"))
  //  val wsClient = AhcWSClient()
  //val hbaseRest = new HBaseRestDataAccess(wsClient, configuration)

  //
  //  "HBaseRestDataAccess" should "get units from HBaseRest" in {
  //    val path = "/sbr_local_db:unit_links/201706~976351836291~*/d"
  //    stubFor(get(urlEqualTo(path))
  //      .willReturn(
  //        aResponse()
  //          .withStatus(200)
  //      ))
  //    val url = s"http://$Host:$Port$path"
  //    val resp = ws.url(url).get()
  //
  //    val response = Await.result(resp, Duration(100, TimeUnit.MILLISECONDS))
  //    response.status should be(200)
  //    1 should equal(1)
  //  }
  //
  //  "HBaseRestDataAccess" should "get units" in {
  //    1 should equal(1)
  //  }

  "Unit Search on HBaseConnect should" should {
    "return a unit for a given id" in {
      1 must equal(1)
    }

    "test" in {
      val search = fakeRequest(s"/v1/units/12345")
      status(search) mustBe NOT_FOUND
      contentType(search) mustBe Some("application/json")
    }
  }
}
