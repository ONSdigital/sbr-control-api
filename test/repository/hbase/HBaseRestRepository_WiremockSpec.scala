package repository.hbase

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.{ PatienceConfiguration, ScalaFutures }
import org.scalatest.time.{ Millis, Span }
import org.scalatest.{ EitherValues, Matchers, OneInstancePerTest, Outcome }
import play.api.http.Port
import play.api.http.Status.{ BAD_REQUEST, NOT_FOUND, SERVICE_UNAVAILABLE, UNAUTHORIZED }
import play.api.libs.json.{ JsError, JsSuccess, Json, Reads }
import play.api.test.WsTestClient
import repository.RestRepository.Row
import support.WithWireMockHBase

class HBaseRestRepository_WiremockSpec extends org.scalatest.fixture.FreeSpec with WithWireMockHBase with Matchers with ScalaFutures with PatienceConfiguration with EitherValues with MockFactory with OneInstancePerTest {

  private val DummyJsonResponseStr = """{"some":"json"}"""
  private val Table = "table"
  private val RowKey = "rowKey"
  private val ColumnFamily = "cg"

  // test timeout must exceed the configured HBaseRest timeout to properly test client-side timeout handling
  override implicit val patienceConfig = PatienceConfig(timeout = scaled(Span(1500, Millis)), interval = scaled(Span(50, Millis)))

  protected case class FixtureParam(
      config: HBaseRestRepositoryConfig,
      auth: Authorization,
      repository: HBaseRestRepository,
      responseReaderMaker: HBaseResponseReaderMaker,
      readsRows: Reads[Seq[Row]]
  ) {
    val targetUrl = s"/${config.namespace}:$Table/$RowKey/$ColumnFamily"
  }

  override protected def withFixture(test: OneArgTest): Outcome = {
    val config = HBaseRestRepositoryConfig(protocolWithHostname = "http://localhost", port = wireMockPort.toString,
      "namespace", "username", "password", timeout = 1000L)
    val auth = Authorization(config.username, config.password)
    val responseReaderMaker = mock[HBaseResponseReaderMaker]
    val readsRows = mock[Reads[Seq[Row]]]

    // OneInstancePerTest is required for this common expectation to work across all of the individual tests
    (responseReaderMaker.forColumnFamily _).expects(ColumnFamily).returning(readsRows)

    WsTestClient.withClient { wsClient =>
      withFixture(test.toNoArgTest(FixtureParam(
        config, auth, new HBaseRestRepository(config, wsClient, responseReaderMaker), responseReaderMaker, readsRows
      )))
    }(new Port(wireMockPort))
  }

  "A HBase REST Repository" - {
    "when expecting to find at most one row" - {
      "can process a valid success response containing a single row" in { fixture =>
        val expectedRow = Map("key1" -> "value1")
        stubHBaseFor(getHBaseJson(fixture.targetUrl, fixture.auth).willReturn(anOkResponse().withBody(DummyJsonResponseStr)))
        (fixture.readsRows.reads _).expects(Json.parse(DummyJsonResponseStr)).returning(JsSuccess(Seq(expectedRow)))

        whenReady(fixture.repository.findRow(Table, RowKey, ColumnFamily)) { result =>
          result.right.value shouldBe Some(expectedRow)
        }
      }

      /*
       * This is the NOT FOUND case when running against Cloudera, which returns an "empty row" (rather than a 404).
       */
      "can process a valid success response containing no rows" in { fixture =>
        stubHBaseFor(getHBaseJson(fixture.targetUrl, fixture.auth).willReturn(anOkResponse().withBody(DummyJsonResponseStr)))
        (fixture.readsRows.reads _).expects(Json.parse(DummyJsonResponseStr)).returning(JsSuccess(Seq.empty))

        whenReady(fixture.repository.findRow(Table, RowKey, ColumnFamily)) { result =>
          result.right.value shouldBe None
        }
      }

      /*
       * Currently only applicable for developers testing directly against a recent version of HBase.
       * This contrasts with the behaviour of Cloudera, which instead returns OK with a representation of an "empty row".
       */
      "can process a NOT FOUND response" in { fixture =>
        stubHBaseFor(getHBaseJson(fixture.targetUrl, fixture.auth).willReturn(aResponse().withStatus(NOT_FOUND)))

        whenReady(fixture.repository.findRow(Table, RowKey, ColumnFamily)) { result =>
          result.right.value shouldBe None
        }
      }

      "fails" - {
        "when multiple results are found" in { fixture =>
          val multipleRows = Seq(Map("key" -> "value1"), Map("key" -> "value2"))
          stubHBaseFor(getHBaseJson(fixture.targetUrl, fixture.auth).willReturn(anOkResponse().withBody(DummyJsonResponseStr)))
          (fixture.readsRows.reads _).expects(Json.parse(DummyJsonResponseStr)).returning(JsSuccess(multipleRows))

          whenReady(fixture.repository.findRow(Table, RowKey, ColumnFamily)) { result =>
            result.left.value shouldBe "At most one result was expected but found [2]"
          }
        }
      }
    }

    "when expecting to find zero to many rows" - {
      "can process a valid success response containing multiple rows" in { fixture =>
        val row1 = Map("key1" -> "value1")
        val row2 = Map("key2" -> "value2")
        stubHBaseFor(getHBaseJson(fixture.targetUrl, fixture.auth).willReturn(anOkResponse().withBody(DummyJsonResponseStr)))
        (fixture.readsRows.reads _).expects(Json.parse(DummyJsonResponseStr)).returning(JsSuccess(Seq(row1, row2)))

        whenReady(fixture.repository.findRows(Table, RowKey, ColumnFamily)) { result =>
          result.right.value should contain theSameElementsAs Seq(row1, row2)
        }
      }
    }

    "fails" - {
      "when the configured user credentials are not accepted" in { fixture =>
        stubHBaseFor(getHBaseJson(fixture.targetUrl, fixture.auth).willReturn(aResponse().withStatus(UNAUTHORIZED)))

        whenReady(fixture.repository.findRows(Table, RowKey, ColumnFamily)) { result =>
          result.left.value shouldBe "Unauthorized (401) - check HBase REST configuration"
        }
      }

      "when the response is a client error" in { fixture =>
        stubHBaseFor(getHBaseJson(fixture.targetUrl, fixture.auth).willReturn(aResponse().withStatus(BAD_REQUEST)))

        whenReady(fixture.repository.findRows(Table, RowKey, ColumnFamily)) { result =>
          result.left.value shouldBe "Bad Request (400)"
        }
      }

      "when the response is a server error" in { fixture =>
        stubHBaseFor(getHBaseJson(fixture.targetUrl, fixture.auth).willReturn(aResponse().withStatus(SERVICE_UNAVAILABLE)))

        whenReady(fixture.repository.findRows(Table, RowKey, ColumnFamily)) { result =>
          result.left.value shouldBe "Service Unavailable (503)"
        }
      }

      "when an OK response is returned containing a non-JSON body" in { fixture =>
        stubHBaseFor(getHBaseJson(fixture.targetUrl, fixture.auth).willReturn(anOkResponse().withBody("this-is-not-json")))

        whenReady(fixture.repository.findRows(Table, RowKey, ColumnFamily)) { result =>
          result.left.value should startWith("Unable to create JsValue from HBase response")
        }
      }

      "when an OK response contains a JSON body that cannot be parsed" in { fixture =>
        stubHBaseFor(getHBaseJson(fixture.targetUrl, fixture.auth).willReturn(anOkResponse().withBody(DummyJsonResponseStr)))
        (fixture.readsRows.reads _).expects(Json.parse(DummyJsonResponseStr)).returning(JsError("parse failure"))

        whenReady(fixture.repository.findRows(Table, RowKey, ColumnFamily)) { result =>
          result.left.value should startWith("Unable to parse HBase REST json response")
        }
      }

      /*
       * Test patienceConfig must exceed the fixedDelay for this to work...
       */
      "when the server takes longer to respond than the configured client-side timeout" in { fixture =>
        stubHBaseFor(getHBaseJson(fixture.targetUrl, fixture.auth).willReturn(anOkResponse().withBody(DummyJsonResponseStr).
          withFixedDelay((fixture.config.timeout + 100).toInt)))

        whenReady(fixture.repository.findRows(Table, RowKey, ColumnFamily)) { result =>
          result.left.value should startWith("Timeout.")
        }
      }
    }
  }
}
