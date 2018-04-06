package repository.hbase

import com.github.tomakehurst.wiremock.client.WireMock
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ Matchers, Outcome }
import play.api.http.Port
import play.api.http.Status.NOT_FOUND
import play.api.libs.json.{ JsSuccess, Json, Reads }
import play.api.test.WsTestClient
import repository.RestRepository.Row
import support.WithWireMockHBase

class HBaseRestRepository_WiremockSpec extends org.scalatest.fixture.FreeSpec with WithWireMockHBase with Matchers with ScalaFutures with MockFactory {

  private val DummyJsonResponseStr = """{"some":"json"}"""
  private val ColumnGroup = "cg"

  protected case class FixtureParam(
    config: HBaseRestRepositoryConfig,
    auth: Authorization,
    repository: HBaseRestRepository,
    responseReaderMaker: HBaseResponseReaderMaker,
    readsRows: Reads[Seq[Row]]
  )

  override protected def withFixture(test: OneArgTest): Outcome = {
    val config = HBaseRestRepositoryConfig(protocolWithHostname = "http://localhost", port = wireMockPort.toString,
      "namespace", "username", "password", timeout = 2000L)
    val auth = Authorization(config.username, config.password)
    val responseReaderMaker = mock[HBaseResponseReaderMaker]
    val readsRows = mock[Reads[Seq[Row]]]

    WsTestClient.withClient { wsClient =>
      withFixture(test.toNoArgTest(FixtureParam(
        config, auth, new HBaseRestRepository(config, wsClient, responseReaderMaker), responseReaderMaker, readsRows
      )))
    }(new Port(wireMockPort))
  }

  "A HBase REST Repository" - {
    /*
     * This also covers the NOT FOUND case when running against Cloudera, which returns an "empty row" to
     * signal not found rather than a 404.
     */
    "can process a success response" in { fixture =>
      val targetUrl = s"/${fixture.config.namespace}:table/rowKey/$ColumnGroup"
      val expectedRow = Map("key1" -> "value1")
      stubHBaseFor(getHbaseJson(targetUrl, fixture.auth).willReturn(anOkResponse().withBody(DummyJsonResponseStr)))
      (fixture.responseReaderMaker.forColumnGroup _).expects(ColumnGroup).returning(fixture.readsRows)
      (fixture.readsRows.reads _).expects(Json.parse(DummyJsonResponseStr)).returning(JsSuccess(Seq(expectedRow)))

      whenReady(fixture.repository.get("table", "rowKey", ColumnGroup)) { result =>
        result shouldBe Seq(expectedRow)
      }
    }

    /*
     * Currently only applicable for developers testing directly against a recent version of HBase.
     * Our current version of Cloudera instead returns OK with a representation of an "empty row".
     */
    "can process a NOT FOUND response" in { fixture =>
      val targetUrl = s"/${fixture.config.namespace}:table/rowKey/$ColumnGroup"
      stubHBaseFor(getHbaseJson(targetUrl, fixture.auth).willReturn(WireMock.aResponse().withStatus(NOT_FOUND)))
      (fixture.responseReaderMaker.forColumnGroup _).expects(ColumnGroup).returning(fixture.readsRows)

      whenReady(fixture.repository.get("table", "rowKey", ColumnGroup)) { result =>
        result shouldBe Seq.empty
      }
    }
  }
}
