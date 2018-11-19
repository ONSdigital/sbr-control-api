package repository.hbase

import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{EitherValues, FreeSpec, Matchers, OneInstancePerTest}
import play.api.http.Status
import play.api.libs.json.{JsSuccess, JsValue, Json, Reads}
import play.api.libs.ws.{BodyWritable, WSClient, WSRequest, WSResponse}
import repository.EditFailed
import repository.RestRepository.Row
import utils.BaseUrl

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

/*
 * This spec mocks the wsClient, which disregards the rule "don't mock types you don't own" (see "Growing
 * Object-Oriented Software, Guided by Tests" by Freeman & Pryce).  Prefer the sibling test that uses Wiremock
 * where possible.  This was introduced to simplify asserting that the client-side timeout is configured correctly,
 * as this is not observable via Wiremock.  It also allows us to assert that the configured host / port are used,
 * as the wsTestClient used by the acceptance test overrides these.
 *
 * True to form, this test was broken by the upgrade to Play 2.6, as request now requires a BodyWritable[T] rather
 * than a Writable[T].
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
    val ColumnName = Column(ColumnFamily, ColumnQualifier)
    val OldValue = "A"
    val NewValue = "B"

    val wsClient = stub[WSClient]
    val wsResponse = stub[WSResponse]
    val hbaseResponseReaderMaker = stub[HBaseResponseReaderMaker]
    val config = HBaseRestRepositoryConfig(
      BaseUrl(protocol = Protocol, host = Host, port = Port, prefix = Some("HBase")),
      "namespace", "username", "password", timeout = ClientTimeout
    )
    val restRepository = new HBaseRestRepository(config, wsClient, hbaseResponseReaderMaker)(ExecutionContext.global)

    val wsRequest = mock[WSRequest]

    def expectRequestHeadersAndAuth(): Unit = {
      (wsRequest.withHttpHeaders _).expects(*).returning(wsRequest)
      (wsRequest.withAuth _).expects(*, *, *).returning(wsRequest)
      () // explicitly return unit to avoid warning about disregarded return value
    }

    def stubTargetRowExists(): Unit = {
      val getRequest = stub[WSRequest]
      val getResponse = stub[WSResponse]
      val body = Json.parse("{}")
      val readsRows = stub[Reads[Seq[Row]]]
      (getRequest.withHttpHeaders _).when(*).returns(getRequest)
      (getRequest.withAuth _).when(*, *, *).returning(getRequest)
      (getRequest.withRequestTimeout _).when(*).returns(getRequest)

      (wsClient.url _).when(where[String] { _.endsWith("/columnFamily") }).returns(getRequest)
      (getRequest.get _).when().returns(Future.successful(getResponse))
      (getResponse.status _).when().returns(Status.OK)
      (getResponse.json _).when().returns(body)
      (hbaseResponseReaderMaker.forColumnFamily _).when(*).returns(readsRows)
      (readsRows.reads _).when(*).returns(JsSuccess(Seq(Row("rowKey", Map(ColumnQualifier -> OldValue)))))
      () // explicitly return unit to avoid warning about disregarded return value
    }
  }

  private trait QueryFixture extends Fixture

  private trait EditFixture extends Fixture {
    val EditUrlSuffix = "/?check=put"
  }

  private trait CreateFixture extends Fixture {
    val CreateUrlSuffix = s"/$RowKey"
  }

  private trait DeleteFixture extends EditFixture {
    val DeleteUrlSuffix = "/?check=delete"
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
        (wsRequest.put(_: JsValue)(_: BodyWritable[JsValue])).expects(*, *).returning(Future.successful(wsResponse))

        Await.result(restRepository.updateField(Table, RowKey, checkField = ColumnName -> OldValue, updateField = ColumnName -> NewValue), AwaitTime)
      }

      "targets the specified host and port when making a request" in new EditFixture {
        stubTargetRowExists()
        (wsClient.url _).when(where[String] { url =>
          url.startsWith(s"$Protocol://$Host:$Port") &&
            url.endsWith(EditUrlSuffix)
        }).returning(wsRequest)
        expectRequestHeadersAndAuth()
        (wsRequest.withRequestTimeout _).expects(*).returning(wsRequest)
        (wsRequest.put(_: JsValue)(_: BodyWritable[JsValue])).expects(*, *).returning(Future.successful(wsResponse))

        Await.result(restRepository.updateField(Table, RowKey, checkField = ColumnName -> OldValue, updateField = ColumnName -> NewValue), AwaitTime)
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
        (wsRequest.put(_: JsValue)(_: BodyWritable[JsValue])).expects(*, *).returning(
          Future.failed(new Exception("Connection failed"))
        )

        whenReady(restRepository.updateField(Table, RowKey, checkField = ColumnName -> OldValue, updateField = ColumnName -> NewValue)) { result =>
          result shouldBe EditFailed
        }
      }
    }

    "when creating data" - {
      "specifies the configured client-side timeout when making a request" in new CreateFixture {
        (wsClient.url _).when(where[String] { _.endsWith(CreateUrlSuffix) }).returns(wsRequest)
        expectRequestHeadersAndAuth()
        (wsRequest.withRequestTimeout _).expects(ClientTimeout.milliseconds).returning(wsRequest)
        (wsRequest.put(_: JsValue)(_: BodyWritable[JsValue])).expects(*, *).returning(Future.successful(wsResponse))

        Await.result(restRepository.createOrReplace(Table, RowKey, field = ColumnName -> NewValue), AwaitTime)
      }

      "targets the specified host and port when making a request" in new CreateFixture {
        (wsClient.url _).when(where[String] { url => url.startsWith(s"$Protocol://$Host:$Port") && url.endsWith(CreateUrlSuffix) }).returning(wsRequest)
        expectRequestHeadersAndAuth()
        (wsRequest.withRequestTimeout _).expects(*).returning(wsRequest)
        (wsRequest.put(_: JsValue)(_: BodyWritable[JsValue])).expects(*, *).returning(Future.successful(wsResponse))

        Await.result(restRepository.createOrReplace(Table, RowKey, field = ColumnName -> NewValue), AwaitTime)
      }

      "materialises an error into a failure" in new CreateFixture {
        (wsClient.url _).when(where[String] { _.endsWith(CreateUrlSuffix) }).returns(wsRequest)
        expectRequestHeadersAndAuth()
        (wsRequest.withRequestTimeout _).expects(*).returning(wsRequest)
        (wsRequest.put(_: JsValue)(_: BodyWritable[JsValue])).expects(*, *).returning(
          Future.failed(new Exception("Connection failed"))
        )

        whenReady(restRepository.createOrReplace(Table, RowKey, field = ColumnName -> NewValue)) { result =>
          result shouldBe EditFailed
        }
      }
    }

    "when deleting data" - {
      "specifies the configured client-side timeout when making a request" in new DeleteFixture {
        stubTargetRowExists()
        (wsClient.url _).when(where[String] { _.endsWith(DeleteUrlSuffix) }).returns(wsRequest)
        expectRequestHeadersAndAuth()
        (wsRequest.withRequestTimeout _).expects(ClientTimeout.milliseconds).returning(wsRequest)
        (wsRequest.put(_: JsValue)(_: BodyWritable[JsValue])).expects(*, *).returning(Future.successful(wsResponse))

        Await.result(restRepository.deleteField(Table, RowKey, checkField = ColumnName -> OldValue, ColumnName), AwaitTime)
      }

      "targets the specified host and port when making a request" in new DeleteFixture {
        stubTargetRowExists()
        (wsClient.url _).when(where[String] { url => url.startsWith(s"$Protocol://$Host:$Port") && url.endsWith(DeleteUrlSuffix) }).returning(wsRequest)
        expectRequestHeadersAndAuth()
        (wsRequest.withRequestTimeout _).expects(*).returning(wsRequest)
        (wsRequest.put(_: JsValue)(_: BodyWritable[JsValue])).expects(*, *).returning(Future.successful(wsResponse))

        Await.result(restRepository.deleteField(Table, RowKey, checkField = ColumnName -> OldValue, ColumnName), AwaitTime)
      }

      "materialises an error into a failure" in new DeleteFixture {
        stubTargetRowExists()
        (wsClient.url _).when(where[String] { _.endsWith(DeleteUrlSuffix) }).returns(wsRequest)
        expectRequestHeadersAndAuth()
        (wsRequest.withRequestTimeout _).expects(*).returning(wsRequest)
        (wsRequest.put(_: JsValue)(_: BodyWritable[JsValue])).expects(*, *).returning(
          Future.failed(new Exception("Connection failed"))
        )

        whenReady(restRepository.deleteField(Table, RowKey, checkField = ColumnName -> OldValue, ColumnName)) { result =>
          result shouldBe EditFailed
        }
      }
    }
  }
}
