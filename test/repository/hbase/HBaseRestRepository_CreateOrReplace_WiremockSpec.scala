package repository.hbase

import com.github.tomakehurst.wiremock.client.WireMock.{ aResponse, equalToJson }
import play.api.http.Status.{ BAD_REQUEST, UNAUTHORIZED }
import repository.{ EditApplied, EditFailed }

class HBaseRestRepository_CreateOrReplace_WiremockSpec extends AbstractEditHBaseRestRepositoryWiremockSpec {

  private val CellValue = "cellValue"
  private val OtherCellValue = "otherCellValue"
  private val OtherColumnName = Column("otherColumnFamily", "otherColumnQualifier")
  private val SingleColumn = Seq(
    aColumnWith(family = ColumnFamily, qualifier = ColumnQualifier, value = CellValue, timestamp = None)
  )
  private val MultipleColumns = SingleColumn :+ aColumnWith(family = OtherColumnName.family, qualifier = OtherColumnName.qualifier,
    value = OtherCellValue, timestamp = None)

  override protected def makeUrl(config: HBaseRestRepositoryConfig): String =
    s"/${config.namespace}:$Table/$RowKey"

  "A HBase REST Repository" - {
    "can successfully" - {
      "create or replace a single cell" in { fixture =>
        stubHBaseFor(putHBaseJson(fixture.targetUrl, fixture.auth).withRequestBody(equalToJson(requestBodyFor(SingleColumn))).willReturn(
          anOkResponse()
        ))

        whenReady(fixture.repository.createOrReplace(Table, RowKey, (ColumnName, CellValue))) { result =>
          result shouldBe EditApplied
        }
      }

      "create or replace multiple cells" in { fixture =>
        stubHBaseFor(putHBaseJson(fixture.targetUrl, fixture.auth).withRequestBody(equalToJson(requestBodyFor(MultipleColumns))).willReturn(
          anOkResponse()
        ))

        whenReady(fixture.repository.createOrReplace(Table, RowKey, (ColumnName, CellValue), (OtherColumnName, OtherCellValue))) { result =>
          result shouldBe EditApplied
        }
      }
    }

    "fails a createOrReplace operation" - {
      /*
       * Test patienceConfig must exceed the fixedDelay for this to work...
       */
      "when the server takes longer to respond than the configured client-side timeout" in { fixture =>
        stubHBaseFor(putHBaseJson(fixture.targetUrl, fixture.auth).withRequestBody(equalToJson(requestBodyFor(SingleColumn))).willReturn(
          anOkResponse().withFixedDelay((fixture.config.timeout + 100).toInt)
        ))

        whenReady(fixture.repository.createOrReplace(Table, RowKey, (ColumnName, CellValue))) { result =>
          result shouldBe EditFailed
        }
      }

      "when the configured user credentials are not accepted" in { fixture =>
        stubHBaseFor(putHBaseJson(fixture.targetUrl, fixture.auth).withRequestBody(equalToJson(requestBodyFor(SingleColumn))).willReturn(
          aResponse().withStatus(UNAUTHORIZED)
        ))

        whenReady(fixture.repository.createOrReplace(Table, RowKey, (ColumnName, CellValue))) { result =>
          result shouldBe EditFailed
        }
      }

      "when the response is a client error" in { fixture =>
        stubHBaseFor(putHBaseJson(fixture.targetUrl, fixture.auth).withRequestBody(equalToJson(requestBodyFor(SingleColumn))).willReturn(
          aResponse().withStatus(BAD_REQUEST)
        ))

        whenReady(fixture.repository.createOrReplace(Table, RowKey, (ColumnName, CellValue))) { result =>
          result shouldBe EditFailed
        }
      }

      "when the response is a server error" in { fixture =>
        stubHBaseFor(putHBaseJson(fixture.targetUrl, fixture.auth).withRequestBody(equalToJson(requestBodyFor(SingleColumn))).willReturn(
          aServiceUnavailableResponse()
        ))

        whenReady(fixture.repository.createOrReplace(Table, RowKey, (ColumnName, CellValue))) { result =>
          result shouldBe EditFailed
        }
      }
    }
  }
}
