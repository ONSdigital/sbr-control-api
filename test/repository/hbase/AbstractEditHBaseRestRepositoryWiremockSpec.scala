package repository.hbase

import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.{PatienceConfiguration, ScalaFutures}
import org.scalatest.time.{Millis, Span}
import org.scalatest.{EitherValues, Matchers, Outcome}
import play.api.http.Port
import play.api.libs.json.{JsResult, JsSuccess, Json, Reads}
import play.api.libs.ws.WSClient
import play.api.test.WsTestClient
import repository.RestRepository.Row
import support.wiremock.WireMockHBase
import utils.BaseUrl

import scala.concurrent.ExecutionContext

abstract class AbstractEditHBaseRestRepositoryWiremockSpec extends org.scalatest.fixture.FreeSpec with WireMockHBase with Matchers with EitherValues with MockFactory with ScalaFutures with PatienceConfiguration {
  val Table = "table"
  val RowKey = "rowKey"
  val ColumnFamily = "columnFamily"
  val ColumnQualifier = "columnQualifier"
  val ColumnName = Column(ColumnFamily, ColumnQualifier)

  // test timeout must exceed the configured HBaseRest timeout to properly test client-side timeout handling
  override implicit val patienceConfig = PatienceConfig(timeout = scaled(Span(1500, Millis)), interval = scaled(Span(50, Millis)))

  protected def makeUrl(config: HBaseRestRepositoryConfig): String

  protected def requestBodyFor(columns: Seq[String]): String =
    s"""{"Row": ${
      List(aRowWith(key = s"$RowKey", columns: _*)).mkString("[", ",", "]")
    }}"""

  protected class FixtureParam(
      val config: HBaseRestRepositoryConfig,
      val auth: Authorization,
      val repository: HBaseRestRepository,
      responseReaderMaker: HBaseResponseReaderMaker,
      val targetUrl: String
  ) {
    def stubTargetEntityIsFound(withFields: Map[String, String] = Map("key1" -> "value1")): Unit = {
      val bodyParseResult = JsSuccess(Seq(Row(rowKey = "UnusedRowKey", withFields)))
      stubTargetEntityRetrieves(bodyParseResult)
    }

    def stubTargetEntityIsNotFound(): Unit =
      stubTargetEntityRetrieves(JsSuccess(Seq.empty))

    private def stubTargetEntityRetrieves(bodyParseResult: JsResult[Seq[Row]]): Unit = {
      val url = s"/${config.namespace}:$Table/$RowKey/$ColumnFamily"
      stubTargetEntityRetrieves(url, bodyParseResult, ColumnFamily)
    }

    private def stubTargetEntityRetrieves(url: String, bodyParseResult: JsResult[Seq[Row]], columnFamily: String): Unit = {
      val DummyJsonResponseStr = """{"some":"json"}"""
      val readsRows = stub[Reads[Seq[Row]]]
      stubHBaseFor(getHBaseJson(url, auth).willReturn(
        anOkResponse().withBody(DummyJsonResponseStr)
      ))
      (responseReaderMaker.forColumnFamily _).when(columnFamily).returns(readsRows)
      (readsRows.reads _).when(Json.parse(DummyJsonResponseStr)).returns(bodyParseResult)
      () // explicitly return unit to avoid warning about disregarded return value
    }
  }

  protected object FixtureParam {
    def apply(
      config: HBaseRestRepositoryConfig,
      auth: Authorization,
      repository: HBaseRestRepository,
      responseReaderMaker: HBaseResponseReaderMaker,
      targetUrl: String
    ): FixtureParam =
      new FixtureParam(config, auth, repository, responseReaderMaker, targetUrl)
  }

  protected def makeFixtureParam(wsClient: WSClient): FixtureParam = {
    val config = HBaseRestRepositoryConfig(
      BaseUrl(protocol = "http", host = "localhost", port = DefaultHBasePort, prefix = None),
      "namespace", "username", "password", timeout = 1000L
    )
    val auth = Authorization(config.username, config.password)
    val responseReaderMaker = stub[HBaseResponseReaderMaker]
    FixtureParam(
      config,
      auth,
      new HBaseRestRepository(config, wsClient, responseReaderMaker)(ExecutionContext.global),
      responseReaderMaker,
      makeUrl(config)
    )
  }

  override protected def withFixture(test: OneArgTest): Outcome =
    withWireMockHBase { () =>
      WsTestClient.withClient { wsClient =>
        withFixture(
          test.toNoArgTest(makeFixtureParam(wsClient))
        )
      }(new Port(DefaultHBasePort))
    }
}
