package repository.hbase

import com.github.tomakehurst.wiremock.client.WireMock.{ aResponse, equalToJson }
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.{ PatienceConfiguration, ScalaFutures }
import org.scalatest.time.{ Millis, Span }
import org.scalatest.{ EitherValues, Matchers, Outcome }
import play.api.http.Port
import play.api.http.Status.{ BAD_REQUEST, UNAUTHORIZED }
import play.api.libs.json.{ JsResult, JsSuccess, Json, Reads }
import play.api.test.WsTestClient
import repository.RestRepository.Row
import repository._
import support.WithWireMockHBase
import utils.BaseUrl

class HBaseRestRepository_Delete_WiremockSpec extends org.scalatest.fixture.FreeSpec with WithWireMockHBase with Matchers with EitherValues with MockFactory with ScalaFutures with PatienceConfiguration {

  private val Table = "table"
  private val RowKey = "rowKey"
  private val ColumnFamily = "columnFamily"
  private val ColumnName = "columnName"
  private val QualifiedColumn = Column(ColumnFamily, ColumnName)
  private val CurrentValue = "currentValue"
  private val DeleteField = Map(ColumnName -> CurrentValue)
  private val MissingDeleteField = Map("foo" -> "bar")
  private val CheckAndDeleteRequestBody =
    s"""{"Row": ${
      List(aRowWith(key = s"$RowKey", columns =
        aColumnWith(family = ColumnFamily, qualifier = ColumnName, value = CurrentValue, timestamp = None))).mkString("[", ",", "]")
    }}"""

  // test timeout must exceed the configured HBaseRest timeout to properly test client-side timeout handling
  override implicit val patienceConfig = PatienceConfig(timeout = scaled(Span(1500, Millis)), interval = scaled(Span(50, Millis)))

  protected case class FixtureParam(
      config: HBaseRestRepositoryConfig,
      auth: Authorization,
      repository: HBaseRestRepository,
      responseReaderMaker: HBaseResponseReaderMaker
  ) {
    val targetUrl = s"/${config.namespace}:$Table/$RowKey/$ColumnFamily:$ColumnName/?check=delete"
  }

  override protected def withFixture(test: OneArgTest): Outcome = {
    val config = HBaseRestRepositoryConfig(
      BaseUrl(protocol = "http", host = "localhost", port = wireMockPort, prefix = None),
      "namespace", "username", "password", timeout = 1000L
    )
    val auth = Authorization(config.username, config.password)
    val responseReaderMaker = stub[HBaseResponseReaderMaker]

    WsTestClient.withClient { wsClient =>
      withFixture(test.toNoArgTest(
        FixtureParam(config, auth, new HBaseRestRepository(config, wsClient, responseReaderMaker), responseReaderMaker)
      ))
    }(new Port(wireMockPort))
  }

  private def stubTargetEntityIsFound(
    config: HBaseRestRepositoryConfig,
    authorization: Authorization,
    responseReaderMaker: HBaseResponseReaderMaker,
    withFields: Map[String, String] = DeleteField
  ): Unit = {
    val bodyParseResult = JsSuccess(Seq(Row(rowKey = "UnusedRowKey", withFields)))
    stubTargetEntityRetrieves(config, authorization, responseReaderMaker, bodyParseResult)
  }

  private def stubTargetEntityIsNotFound(
    config: HBaseRestRepositoryConfig,
    authorization: Authorization,
    responseReaderMaker: HBaseResponseReaderMaker
  ): Unit =
    stubTargetEntityRetrieves(config, authorization, responseReaderMaker, JsSuccess(Seq.empty))

  private def stubTargetEntityRetrieves(
    config: HBaseRestRepositoryConfig,
    authorization: Authorization,
    responseReaderMaker: HBaseResponseReaderMaker,
    bodyParseResult: JsResult[Seq[Row]]
  ): Unit = {
    val DummyJsonResponseStr = """{"some":"json"}"""
    val readsRows = stub[Reads[Seq[Row]]]

    stubHBaseFor(getHBaseJson(s"/${config.namespace}:$Table/$RowKey/$ColumnFamily", authorization).willReturn(
      anOkResponse().withBody(DummyJsonResponseStr)
    ))
    (responseReaderMaker.forColumnFamily _).when(ColumnFamily).returns(readsRows)
    (readsRows.reads _).when(Json.parse(DummyJsonResponseStr)).returns(bodyParseResult)
    () // explicitly return unit to avoid warning about disregarded return value
  }

  "A HBase REST Repository" - {
    "successfully applies a delete" - {
      "when the column exists and its value has not been modified by another user" in { fixture =>
        stubTargetEntityIsFound(fixture.config, fixture.auth, fixture.responseReaderMaker)
        stubHBaseFor(putHBaseJson(fixture.targetUrl, fixture.auth).withRequestBody(equalToJson(CheckAndDeleteRequestBody)).willReturn(
          anOkResponse()
        ))

        whenReady(fixture.repository.deleteField(Table, RowKey, checkField = (QualifiedColumn, CurrentValue), columnName = QualifiedColumn)) { result =>
          result shouldBe EditApplied
        }
      }

      /*
       * In this case we assume that the delete is part of a compound action that is being retried for some reason,
       * when the delete step had in fact succeeded on that previous attempt; or simply that another user has managed
       * to delete the column ahead of us.
       * To be idempotent (and thus a safe to retry operation) we need to treat this as a success scenario.
       */
      "when the column does not exist" in { fixture =>
        stubTargetEntityIsFound(fixture.config, fixture.auth, fixture.responseReaderMaker, withFields = MissingDeleteField)
        // a put call should NOT be made in this scenario
        // this is here to avoid a false positive - as the test framework currently retains prior stubbing between tests ...
        stubHBaseFor(putHBaseJson(fixture.targetUrl, fixture.auth).withRequestBody(equalToJson(CheckAndDeleteRequestBody)).willReturn(
          aServiceUnavailableResponse()
        ))

        whenReady(fixture.repository.deleteField(Table, RowKey, checkField = (QualifiedColumn, CurrentValue), columnName = QualifiedColumn)) { result =>
          result shouldBe EditApplied
        }
      }
    }

    "prevents a delete" - {
      "when the value has been modified by another user" in { fixture =>
        stubTargetEntityIsFound(fixture.config, fixture.auth, fixture.responseReaderMaker)
        stubHBaseFor(putHBaseJson(fixture.targetUrl, fixture.auth).withRequestBody(equalToJson(CheckAndDeleteRequestBody)).willReturn(
          aNotModifiedResponse()
        ))

        whenReady(fixture.repository.deleteField(Table, RowKey, checkField = (QualifiedColumn, CurrentValue), columnName = QualifiedColumn)) { result =>
          result shouldBe EditConflicted
        }
      }
    }

    "rejects a delete" - {
      "when the target entity does not exist" in { fixture =>
        stubTargetEntityIsNotFound(fixture.config, fixture.auth, fixture.responseReaderMaker)

        whenReady(fixture.repository.deleteField(Table, RowKey, checkField = (QualifiedColumn, CurrentValue), columnName = QualifiedColumn)) { result =>
          result shouldBe EditTargetNotFound
        }
      }
    }

    "fails a delete" - {
      /*
       * Test patienceConfig must exceed the fixedDelay for this to work...
       */
      "when the server takes longer to respond than the configured client-side timeout" in { fixture =>
        stubTargetEntityIsFound(fixture.config, fixture.auth, fixture.responseReaderMaker)
        stubHBaseFor(putHBaseJson(fixture.targetUrl, fixture.auth).withRequestBody(equalToJson(CheckAndDeleteRequestBody)).willReturn(
          anOkResponse().withFixedDelay((fixture.config.timeout + 100).toInt)
        ))

        whenReady(fixture.repository.deleteField(Table, RowKey, checkField = (QualifiedColumn, CurrentValue), columnName = QualifiedColumn)) { result =>
          result shouldBe EditFailed
        }
      }

      "when the attempt to retrieve the entity to be modified fails" in { fixture =>
        stubHBaseFor(getHBaseJson(s"/${fixture.config.namespace}:$Table/$RowKey/$ColumnFamily", fixture.auth).willReturn(
          aServiceUnavailableResponse()
        ))

        whenReady(fixture.repository.deleteField(Table, RowKey, checkField = (QualifiedColumn, CurrentValue), columnName = QualifiedColumn)) { result =>
          result shouldBe EditFailed
        }
      }

      "when the configured user credentials are not accepted" in { fixture =>
        stubTargetEntityIsFound(fixture.config, fixture.auth, fixture.responseReaderMaker)
        stubHBaseFor(putHBaseJson(fixture.targetUrl, fixture.auth).withRequestBody(equalToJson(CheckAndDeleteRequestBody)).willReturn(
          aResponse().withStatus(UNAUTHORIZED)
        ))

        whenReady(fixture.repository.deleteField(Table, RowKey, checkField = (QualifiedColumn, CurrentValue), columnName = QualifiedColumn)) { result =>
          result shouldBe EditFailed
        }
      }

      "when the response is a client error" in { fixture =>
        stubTargetEntityIsFound(fixture.config, fixture.auth, fixture.responseReaderMaker)
        stubHBaseFor(putHBaseJson(fixture.targetUrl, fixture.auth).withRequestBody(equalToJson(CheckAndDeleteRequestBody)).willReturn(
          aResponse().withStatus(BAD_REQUEST)
        ))

        whenReady(fixture.repository.deleteField(Table, RowKey, checkField = (QualifiedColumn, CurrentValue), columnName = QualifiedColumn)) { result =>
          result shouldBe EditFailed
        }
      }

      "when the response is a server error" in { fixture =>
        stubTargetEntityIsFound(fixture.config, fixture.auth, fixture.responseReaderMaker)
        stubHBaseFor(putHBaseJson(fixture.targetUrl, fixture.auth).withRequestBody(equalToJson(CheckAndDeleteRequestBody)).willReturn(
          aServiceUnavailableResponse()
        ))

        whenReady(fixture.repository.deleteField(Table, RowKey, checkField = (QualifiedColumn, CurrentValue), columnName = QualifiedColumn)) { result =>
          result shouldBe EditFailed
        }
      }
    }
  }
}
