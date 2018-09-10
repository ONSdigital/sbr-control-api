package repository.hbase

import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ EitherValues, FreeSpec, Matchers, OneInstancePerTest }
import play.api.http.{ Status, Writeable }
import play.api.libs.json.{ JsSuccess, JsValue, Json, Reads }
import play.api.libs.ws.{ WSClient, WSRequest, WSResponse }
import repository.RestRepository.Row
import repository.UpdateFailed
import utils.BaseUrl

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._

/*
 * This spec mocks the wsClient, which disregards the rule "don't mock types you don't own" (see "Growing
 * Object-Oriented Software, Guided by Tests" by Freeman & Pryce).  Prefer the sibling test that uses Wiremock
 * where possible.  This was introduced to simplify asserting that the client-side timeout is configured correctly,
 * as this is not observable via Wiremock.  It also allows us to assert that the configured host / port are used,
 * as the wsTestClient used by the acceptance test overrides these.
 */
class HBaseRestRepository_MockClientSpec extends FreeSpec with Matchers with MockFactory with ScalaFutures with EitherValues with OneInstancePerTest {

  private trait Fixture {
    val Protocol = "http"
    val Host = "somehost"
    val Port = 4321
    val ClientTimeout = 3321L
    val AwaitTime = 500.milliseconds
    val Table = "table"
    val RowKey = "rowKey"
    val ColumnFamily = "columnFamily"
    val ColumnQualifier = "columnQualifier"

    val wsClient = stub[WSClient]
    val wsResponse = stub[WSResponse]
    val hbaseResponseReaderMaker = stub[HBaseResponseReaderMaker]
    val config = HBaseRestRepositoryConfig(
      BaseUrl(protocol = Protocol, host = Host, port = Port, prefix = Some("HBase")),
      "namespace", "username", "password", timeout = ClientTimeout
    )
    val restRepository = new HBaseRestRepository(config, wsClient, hbaseResponseReaderMaker)

    val wsRequest = mock[WSRequest]

    def expectRequestHeadersAndAuth(): Unit = {
      (wsRequest.withHeaders _).expects(*).returning(wsRequest)
      (wsRequest.withAuth _).expects(*, *, *).returning(wsRequest)
      () // explicitly return unit to avoid warning about disregarded return value
    }
  }

  private trait QueryFixture extends Fixture

  private trait EditFixture extends Fixture {
    val EditUrlSuffix = "/?check=put"

    def stubTargetRowExists(): Unit = {
      val getRequest = stub[WSRequest]
      val getResponse = stub[WSResponse]
      val body = Json.parse("{}")
      val readsRows = stub[Reads[Seq[Row]]]
      (getRequest.withHeaders _).when(*).returns(getRequest)
      (getRequest.withAuth _).when(*, *, *).returning(getRequest)
      (getRequest.withRequestTimeout _).when(*).returns(getRequest)

      (wsClient.url _).when(where[String] { _.endsWith("/columnFamily") }).returns(getRequest)
      (getRequest.get _).when().returns(Future.successful(getResponse))
      (getResponse.status _).when().returns(Status.OK)
      (getResponse.json _).when().returns(body)
      (hbaseResponseReaderMaker.forColumnFamily _).when(*).returns(readsRows)
      (readsRows.reads _).when(*).returns(JsSuccess(Seq(Row("rowKey", Map.empty))))
      () // explicitly return unit to avoid warning about disregarded return value
    }
  }

  "A HBase REST repository" - {
    "when querying data" - {
      "specifies the configured client-side timeout when making a request" in new QueryFixture {
        (wsClient.url _).when(*).returns(wsRequest)
        expectRequestHeadersAndAuth()
        (wsRequest.withRequestTimeout _).expects(ClientTimeout.milliseconds).returning(wsRequest)
        (wsRequest.get _).expects().returning(Future.successful(wsResponse))

        Await.result(restRepository.findRows(Table, query = RowKey, ColumnFamily), AwaitTime)
      }

      "targets the specified host and port when making a request" in new QueryFixture {
        (wsClient.url _).when(where[String](_.startsWith(s"$Protocol://$Host:$Port"))).returning(wsRequest)
        expectRequestHeadersAndAuth()
        (wsRequest.withRequestTimeout _).expects(*).returning(wsRequest)
        (wsRequest.get _).expects().returning(Future.successful(wsResponse))

        Await.result(restRepository.findRows(Table, query = RowKey, ColumnFamily), AwaitTime)
      }

      /*
       * Any connection failed / socket disconnected type issue will likely result in the WsRequest's
       * Future failing.  This tests the "catch-all" case, and that we can effectively recover the Future.
       */
      "materialises an error into a failure" in new QueryFixture {
        (wsClient.url _).when(*).returns(wsRequest)
        expectRequestHeadersAndAuth()
        (wsRequest.withRequestTimeout _).expects(*).returning(wsRequest)
        (wsRequest.get _).expects().returning(Future.failed(new Exception("Connection failed")))

        whenReady(restRepository.findRows(Table, query = RowKey, ColumnFamily)) { result =>
          result.left.value shouldBe "Connection failed"
        }
      }
    }

    "when updating data" - {
      "specifies the configured client-side timeout when making a request" in new EditFixture {
        stubTargetRowExists()
        (wsClient.url _).when(where[String] { _.endsWith(EditUrlSuffix) }).returns(wsRequest)
        expectRequestHeadersAndAuth()
        (wsRequest.withRequestTimeout _).expects(ClientTimeout.milliseconds).returning(wsRequest)
        (wsRequest.put(_: JsValue)(_: Writeable[JsValue])).expects(*, *).returning(Future.successful(wsResponse))

        Await.result(restRepository.update(Table, RowKey, checkField = s"$ColumnFamily:$ColumnQualifier" -> "A",
          updateField = s"$ColumnFamily:$ColumnQualifier" -> "B"), AwaitTime)
      }

      "targets the specified host and port when making a request" in new EditFixture {
        stubTargetRowExists()
        (wsClient.url _).when(where[String] { url =>
          url.startsWith(s"$Protocol://$Host:$Port") &&
            url.endsWith(EditUrlSuffix)
        }).returning(wsRequest)
        expectRequestHeadersAndAuth()
        (wsRequest.withRequestTimeout _).expects(*).returning(wsRequest)
        (wsRequest.put(_: JsValue)(_: Writeable[JsValue])).expects(*, *).returning(Future.successful(wsResponse))

        Await.result(restRepository.update(Table, RowKey, checkField = s"$ColumnFamily:$ColumnQualifier" -> "A",
          updateField = s"$ColumnFamily:$ColumnQualifier" -> "B"), AwaitTime)
      }

      /*
       * Any connection failed / socket disconnected type issue will likely result in the WsRequest's
       * Future failing.  This tests the "catch-all" case, and that we can effectively recover the Future.
       */
      "materialises an error into a failure" in new EditFixture {
        stubTargetRowExists()
        (wsClient.url _).when(where[String] { _.endsWith(EditUrlSuffix) }).returns(wsRequest)
        expectRequestHeadersAndAuth()
        (wsRequest.withRequestTimeout _).expects(*).returning(wsRequest)
        (wsRequest.put(_: JsValue)(_: Writeable[JsValue])).expects(*, *).returning(
          Future.failed(new Exception("Connection failed"))
        )

        whenReady(restRepository.update(Table, RowKey, checkField = s"$ColumnFamily:$ColumnQualifier" -> "A",
          updateField = s"$ColumnFamily:$ColumnQualifier" -> "B")) { result =>
          result shouldBe UpdateFailed
        }
      }
    }
  }
}
