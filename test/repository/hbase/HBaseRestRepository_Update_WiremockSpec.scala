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

class HBaseRestRepository_Update_WiremockSpec extends org.scalatest.fixture.FreeSpec with WithWireMockHBase with Matchers with EitherValues with MockFactory with ScalaFutures with PatienceConfiguration {

  private val Table = "table"
  private val RowKey = "rowKey"
  private val ColumnFamily = "columnFamily"
  private val ColumnQualifier = "columnQualifier"
  private val ColumnName = Column(ColumnFamily, ColumnQualifier)
  private val NewValue = "newValue"
  private val OldValue = "oldValue"
  private val OtherColumnName = Column("otherColumnFamily", "otherColumnQualifier")
  private val OtherCellValue = "otherCellValue"
  private val SingleValueCheckAndUpdateRequestBody = requestBodyFor(
    aColumnWith(family = ColumnFamily, qualifier = ColumnQualifier, value = NewValue, timestamp = None),
    aColumnWith(family = ColumnFamily, qualifier = ColumnQualifier, value = OldValue, timestamp = None)
  )
  private val MultipleValueCheckAndUpdateRequestBody = requestBodyFor(
    aColumnWith(family = ColumnFamily, qualifier = ColumnQualifier, value = NewValue, timestamp = None),
    aColumnWith(family = OtherColumnName.family, qualifier = OtherColumnName.qualifier, value = OtherCellValue, timestamp = None),
    aColumnWith(family = ColumnFamily, qualifier = ColumnQualifier, value = OldValue, timestamp = None)
  )

  private def requestBodyFor(columns: String*): String =
    s"""{"Row": ${
      List(aRowWith(key = s"$RowKey", columns: _*)).mkString("[", ",", "]")
    }}"""

  // test timeout must exceed the configured HBaseRest timeout to properly test client-side timeout handling
  override implicit val patienceConfig = PatienceConfig(timeout = scaled(Span(1500, Millis)), interval = scaled(Span(50, Millis)))

  protected case class FixtureParam(
      config: HBaseRestRepositoryConfig,
      auth: Authorization,
      repository: HBaseRestRepository,
      responseReaderMaker: HBaseResponseReaderMaker
  ) {
    val targetUrl = s"/${config.namespace}:$Table/$RowKey/?check=put"
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
    responseReaderMaker: HBaseResponseReaderMaker
  ): Unit = {
    val bodyParseResult = JsSuccess(Seq(Row(rowKey = "UnusedRowKey", fields = Map("key1" -> "value1"))))
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
    "successfully applies an update to a row that has not been modified by another user" - {
      "when the update affects a single field" in { fixture =>
        stubTargetEntityIsFound(fixture.config, fixture.auth, fixture.responseReaderMaker)
        stubHBaseFor(putHBaseJson(fixture.targetUrl, fixture.auth).withRequestBody(equalToJson(SingleValueCheckAndUpdateRequestBody)).willReturn(
          anOkResponse()
        ))

        whenReady(fixture.repository.updateField(Table, RowKey, (ColumnName, OldValue), (ColumnName, NewValue))) { result =>
          result shouldBe EditApplied
        }
      }

      "when the update affects multiple fields" in { fixture =>
        stubTargetEntityIsFound(fixture.config, fixture.auth, fixture.responseReaderMaker)
        /*
         * this should never be called - but is required to prevent the expectation from the single field scenario from
         * allowing this test to pass with an incorrect implementation (we can remove this once the test framework is fixed)
         */
        stubHBaseFor(putHBaseJson(fixture.targetUrl, fixture.auth).withRequestBody(equalToJson(SingleValueCheckAndUpdateRequestBody)).willReturn(
          aServiceUnavailableResponse()
        ))
        stubHBaseFor(putHBaseJson(fixture.targetUrl, fixture.auth).withRequestBody(equalToJson(MultipleValueCheckAndUpdateRequestBody)).willReturn(
          anOkResponse()
        ))

        whenReady(fixture.repository.updateField(Table, RowKey, (ColumnName, OldValue), (ColumnName, NewValue), (OtherColumnName, OtherCellValue))) { result =>
          result shouldBe EditApplied
        }
      }
    }

    "prevents an update" - {
      "when the value has been modified by another user" in { fixture =>
        stubTargetEntityIsFound(fixture.config, fixture.auth, fixture.responseReaderMaker)
        stubHBaseFor(putHBaseJson(fixture.targetUrl, fixture.auth).withRequestBody(equalToJson(SingleValueCheckAndUpdateRequestBody)).willReturn(
          aNotModifiedResponse()
        ))

        whenReady(fixture.repository.updateField(Table, RowKey, (ColumnName, OldValue), (ColumnName, NewValue))) { result =>
          result shouldBe EditConflicted
        }
      }
    }

    "rejects an update" - {
      "when the target entity does not exist" in { fixture =>
        stubTargetEntityIsNotFound(fixture.config, fixture.auth, fixture.responseReaderMaker)

        whenReady(fixture.repository.updateField(Table, RowKey, (ColumnName, OldValue), (ColumnName, NewValue))) { result =>
          result shouldBe EditTargetNotFound
        }
      }
    }

    "fails an update" - {
      /*
       * Test patienceConfig must exceed the fixedDelay for this to work...
       */
      "when the server takes longer to respond than the configured client-side timeout" in { fixture =>
        stubTargetEntityIsFound(fixture.config, fixture.auth, fixture.responseReaderMaker)
        stubHBaseFor(putHBaseJson(fixture.targetUrl, fixture.auth).withRequestBody(equalToJson(SingleValueCheckAndUpdateRequestBody)).willReturn(
          anOkResponse().withFixedDelay((fixture.config.timeout + 100).toInt)
        ))

        whenReady(fixture.repository.updateField(Table, RowKey, (ColumnName, OldValue), (ColumnName, NewValue))) { result =>
          result shouldBe EditFailed
        }
      }

      "when the attempt to retrieve the entity to be modified fails" in { fixture =>
        stubHBaseFor(getHBaseJson(s"/${fixture.config.namespace}:$Table/$RowKey/$ColumnFamily", fixture.auth).willReturn(
          aServiceUnavailableResponse()
        ))

        whenReady(fixture.repository.updateField(Table, RowKey, (ColumnName, OldValue), (ColumnName, NewValue))) { result =>
          result shouldBe EditFailed
        }
      }

      "when the configured user credentials are not accepted" in { fixture =>
        stubTargetEntityIsFound(fixture.config, fixture.auth, fixture.responseReaderMaker)
        stubHBaseFor(putHBaseJson(fixture.targetUrl, fixture.auth).withRequestBody(equalToJson(SingleValueCheckAndUpdateRequestBody)).willReturn(
          aResponse().withStatus(UNAUTHORIZED)
        ))

        whenReady(fixture.repository.updateField(Table, RowKey, (ColumnName, OldValue), (ColumnName, NewValue))) { result =>
          result shouldBe EditFailed
        }
      }

      "when the response is a client error" in { fixture =>
        stubTargetEntityIsFound(fixture.config, fixture.auth, fixture.responseReaderMaker)
        stubHBaseFor(putHBaseJson(fixture.targetUrl, fixture.auth).withRequestBody(equalToJson(SingleValueCheckAndUpdateRequestBody)).willReturn(
          aResponse().withStatus(BAD_REQUEST)
        ))

        whenReady(fixture.repository.updateField(Table, RowKey, (ColumnName, OldValue), (ColumnName, NewValue))) { result =>
          result shouldBe EditFailed
        }
      }

      "when the response is a server error" in { fixture =>
        stubTargetEntityIsFound(fixture.config, fixture.auth, fixture.responseReaderMaker)
        stubHBaseFor(putHBaseJson(fixture.targetUrl, fixture.auth).withRequestBody(equalToJson(SingleValueCheckAndUpdateRequestBody)).willReturn(
          aServiceUnavailableResponse()
        ))

        whenReady(fixture.repository.updateField(Table, RowKey, (ColumnName, OldValue), (ColumnName, NewValue))) { result =>
          result shouldBe EditFailed
        }
      }
    }
  }
}
