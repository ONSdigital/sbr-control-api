package repository.hbase

import com.github.tomakehurst.wiremock.client.WireMock.{ aResponse, equalToJson }
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.{ PatienceConfiguration, ScalaFutures }
import org.scalatest.time.{ Millis, Span }
import org.scalatest.{ EitherValues, Matchers, Outcome }
import play.api.http.Port
import play.api.http.Status.{ BAD_REQUEST, UNAUTHORIZED }
import play.api.test.WsTestClient
import repository.{ EditApplied, EditFailed }
import support.WithWireMockHBase
import utils.BaseUrl

class HBaseRestRepository_CreateOrReplace_WiremockSpec extends org.scalatest.fixture.FreeSpec with WithWireMockHBase with Matchers with EitherValues with MockFactory with ScalaFutures with PatienceConfiguration {

  private val Table = "table"
  private val RowKey = "rowKey"
  private val ColumnName = Column("columnFamily", "columnQualifier")
  private val CellValue = "cellValue"
  private val CreateRequestBody =
    s"""{"Row": ${
      List(aRowWith(key = s"$RowKey", columns =
        aColumnWith(family = ColumnName.family, qualifier = ColumnName.qualifier, value = CellValue, timestamp = None))).mkString("[", ",", "]")
    }}"""

  // test timeout must exceed the configured HBaseRest timeout to properly test client-side timeout handling
  override implicit val patienceConfig = PatienceConfig(timeout = scaled(Span(1500, Millis)), interval = scaled(Span(50, Millis)))

  protected case class FixtureParam(
      config: HBaseRestRepositoryConfig,
      auth: Authorization,
      repository: HBaseRestRepository,
      responseReaderMaker: HBaseResponseReaderMaker
  ) {
    val targetUrl = s"/${config.namespace}:$Table/$RowKey"
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

  "A HBase REST Repository" - {
    "can successfully create or replace a new cell" in { fixture =>
      stubHBaseFor(putHBaseJson(fixture.targetUrl, fixture.auth).withRequestBody(equalToJson(CreateRequestBody)).willReturn(
        anOkResponse()
      ))

      whenReady(fixture.repository.createOrReplace(Table, RowKey, (ColumnName, CellValue))) { result =>
        result shouldBe EditApplied
      }
    }

    "fails a createOrReplace operation" - {
      /*
       * Test patienceConfig must exceed the fixedDelay for this to work...
       */
      "when the server takes longer to respond than the configured client-side timeout" in { fixture =>
        stubHBaseFor(putHBaseJson(fixture.targetUrl, fixture.auth).withRequestBody(equalToJson(CreateRequestBody)).willReturn(
          anOkResponse().withFixedDelay((fixture.config.timeout + 100).toInt)
        ))

        whenReady(fixture.repository.createOrReplace(Table, RowKey, (ColumnName, CellValue))) { result =>
          result shouldBe EditFailed
        }
      }

      "when the configured user credentials are not accepted" in { fixture =>
        stubHBaseFor(putHBaseJson(fixture.targetUrl, fixture.auth).withRequestBody(equalToJson(CreateRequestBody)).willReturn(
          aResponse().withStatus(UNAUTHORIZED)
        ))

        whenReady(fixture.repository.createOrReplace(Table, RowKey, (ColumnName, CellValue))) { result =>
          result shouldBe EditFailed
        }
      }

      "when the response is a client error" in { fixture =>
        stubHBaseFor(putHBaseJson(fixture.targetUrl, fixture.auth).withRequestBody(equalToJson(CreateRequestBody)).willReturn(
          aResponse().withStatus(BAD_REQUEST)
        ))

        whenReady(fixture.repository.createOrReplace(Table, RowKey, (ColumnName, CellValue))) { result =>
          result shouldBe EditFailed
        }
      }

      "when the response is a server error" in { fixture =>
        stubHBaseFor(putHBaseJson(fixture.targetUrl, fixture.auth).withRequestBody(equalToJson(CreateRequestBody)).willReturn(
          aServiceUnavailableResponse()
        ))

        whenReady(fixture.repository.createOrReplace(Table, RowKey, (ColumnName, CellValue))) { result =>
          result shouldBe EditFailed
        }
      }
    }
  }
}
