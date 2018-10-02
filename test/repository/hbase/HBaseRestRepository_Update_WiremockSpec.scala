package repository.hbase

import com.github.tomakehurst.wiremock.client.WireMock.{ aResponse, equalToJson }
import play.api.http.Status.{ BAD_REQUEST, UNAUTHORIZED }
import repository._

class HBaseRestRepository_Update_WiremockSpec extends AbstractEditHBaseRestRepositoryWiremockSpec {

  private val NewValue = "newValue"
  private val OldValue = "oldValue"
  private val OtherColumnName = Column("otherColumnFamily", "otherColumnQualifier")
  private val OtherCellValue = "otherCellValue"

  private val SingleValueCheckAndUpdateRequestBody = requestBodyFor(Seq(
    aColumnWith(family = ColumnFamily, qualifier = ColumnQualifier, value = NewValue, timestamp = None),
    aColumnWith(family = ColumnFamily, qualifier = ColumnQualifier, value = OldValue, timestamp = None)
  ))

  private val MultipleValueCheckAndUpdateRequestBody = requestBodyFor(Seq(
    aColumnWith(family = ColumnFamily, qualifier = ColumnQualifier, value = NewValue, timestamp = None),
    aColumnWith(family = OtherColumnName.family, qualifier = OtherColumnName.qualifier, value = OtherCellValue, timestamp = None),
    aColumnWith(family = ColumnFamily, qualifier = ColumnQualifier, value = OldValue, timestamp = None)
  ))

  override protected def makeUrl(config: HBaseRestRepositoryConfig): String =
    s"/${config.namespace}:$Table/$RowKey/?check=put"

  "A HBase REST Repository" - {
    "successfully applies an update to a row that has not been modified by another user" - {
      "when the update affects a single field" in { fixture =>
        fixture.stubTargetEntityIsFound()
        stubHBaseFor(putHBaseJson(fixture.targetUrl, fixture.auth).withRequestBody(equalToJson(SingleValueCheckAndUpdateRequestBody)).willReturn(
          anOkResponse()
        ))

        whenReady(fixture.repository.updateField(Table, RowKey, (ColumnName, OldValue), (ColumnName, NewValue))) { result =>
          result shouldBe EditApplied
        }
      }

      "when the update affects multiple fields" in { fixture =>
        fixture.stubTargetEntityIsFound()
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
        fixture.stubTargetEntityIsFound()
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
        fixture.stubTargetEntityIsNotFound()

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
        fixture.stubTargetEntityIsFound()
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
        fixture.stubTargetEntityIsFound()
        stubHBaseFor(putHBaseJson(fixture.targetUrl, fixture.auth).withRequestBody(equalToJson(SingleValueCheckAndUpdateRequestBody)).willReturn(
          aResponse().withStatus(UNAUTHORIZED)
        ))

        whenReady(fixture.repository.updateField(Table, RowKey, (ColumnName, OldValue), (ColumnName, NewValue))) { result =>
          result shouldBe EditFailed
        }
      }

      "when the response is a client error" in { fixture =>
        fixture.stubTargetEntityIsFound()
        stubHBaseFor(putHBaseJson(fixture.targetUrl, fixture.auth).withRequestBody(equalToJson(SingleValueCheckAndUpdateRequestBody)).willReturn(
          aResponse().withStatus(BAD_REQUEST)
        ))

        whenReady(fixture.repository.updateField(Table, RowKey, (ColumnName, OldValue), (ColumnName, NewValue))) { result =>
          result shouldBe EditFailed
        }
      }

      "when the response is a server error" in { fixture =>
        fixture.stubTargetEntityIsFound()
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
