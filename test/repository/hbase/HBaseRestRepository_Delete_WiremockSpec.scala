package repository.hbase

import com.github.tomakehurst.wiremock.client.WireMock.{ aResponse, equalToJson }
import play.api.http.Status.{ BAD_REQUEST, UNAUTHORIZED }
import repository._

class HBaseRestRepository_Delete_WiremockSpec extends AbstractEditHBaseRestRepositoryWiremockSpec {

  private val CurrentValue = "currentValue"
  private val DeleteField = Map(ColumnQualifier -> CurrentValue)
  private val MissingDeleteField = Map("foo" -> "bar")
  private val CheckAndDeleteRequestBody = requestBodyFor(Seq(
    aColumnWith(family = ColumnFamily, qualifier = ColumnQualifier, value = CurrentValue, timestamp = None)
  ))

  override protected def makeUrl(config: HBaseRestRepositoryConfig): String =
    s"/${config.namespace}:$Table/$RowKey/$ColumnFamily:$ColumnQualifier/?check=delete"

  "A HBase REST Repository" - {
    "successfully applies a delete" - {
      "when the column exists and its value has not been modified by another user" in { fixture =>
        fixture.stubTargetEntityIsFound(withFields = DeleteField)
        stubHBaseFor(putHBaseJson(fixture.targetUrl, fixture.auth).withRequestBody(equalToJson(CheckAndDeleteRequestBody)).willReturn(
          anOkResponse()
        ))

        whenReady(fixture.repository.deleteField(Table, RowKey, checkField = (ColumnName, CurrentValue), columnName = ColumnName)) { result =>
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
        fixture.stubTargetEntityIsFound(withFields = MissingDeleteField)

        whenReady(fixture.repository.deleteField(Table, RowKey, checkField = (ColumnName, CurrentValue), columnName = ColumnName)) { result =>
          result shouldBe EditApplied
        }
      }
    }

    "prevents a delete" - {
      "when the value has been modified by another user" in { fixture =>
        fixture.stubTargetEntityIsFound(withFields = DeleteField)
        stubHBaseFor(putHBaseJson(fixture.targetUrl, fixture.auth).withRequestBody(equalToJson(CheckAndDeleteRequestBody)).willReturn(
          aNotModifiedResponse()
        ))

        whenReady(fixture.repository.deleteField(Table, RowKey, checkField = (ColumnName, CurrentValue), columnName = ColumnName)) { result =>
          result shouldBe EditConflicted
        }
      }
    }

    "rejects a delete" - {
      "when the target entity does not exist" in { fixture =>
        fixture.stubTargetEntityIsNotFound()

        whenReady(fixture.repository.deleteField(Table, RowKey, checkField = (ColumnName, CurrentValue), columnName = ColumnName)) { result =>
          result shouldBe EditTargetNotFound
        }
      }
    }

    "fails a delete" - {
      /*
       * Test patienceConfig must exceed the fixedDelay for this to work...
       */
      "when the server takes longer to respond than the configured client-side timeout" in { fixture =>
        fixture.stubTargetEntityIsFound(withFields = DeleteField)
        stubHBaseFor(putHBaseJson(fixture.targetUrl, fixture.auth).withRequestBody(equalToJson(CheckAndDeleteRequestBody)).willReturn(
          anOkResponse().withFixedDelay((fixture.config.timeout + 100).toInt)
        ))

        whenReady(fixture.repository.deleteField(Table, RowKey, checkField = (ColumnName, CurrentValue), columnName = ColumnName)) { result =>
          result shouldBe EditFailed
        }
      }

      "when the attempt to retrieve the entity to be modified fails" in { fixture =>
        stubHBaseFor(getHBaseJson(s"/${fixture.config.namespace}:$Table/$RowKey/$ColumnFamily", fixture.auth).willReturn(
          aServiceUnavailableResponse()
        ))

        whenReady(fixture.repository.deleteField(Table, RowKey, checkField = (ColumnName, CurrentValue), columnName = ColumnName)) { result =>
          result shouldBe EditFailed
        }
      }

      "when the configured user credentials are not accepted" in { fixture =>
        fixture.stubTargetEntityIsFound(withFields = DeleteField)
        stubHBaseFor(putHBaseJson(fixture.targetUrl, fixture.auth).withRequestBody(equalToJson(CheckAndDeleteRequestBody)).willReturn(
          aResponse().withStatus(UNAUTHORIZED)
        ))

        whenReady(fixture.repository.deleteField(Table, RowKey, checkField = (ColumnName, CurrentValue), columnName = ColumnName)) { result =>
          result shouldBe EditFailed
        }
      }

      "when the response is a client error" in { fixture =>
        fixture.stubTargetEntityIsFound(withFields = DeleteField)
        stubHBaseFor(putHBaseJson(fixture.targetUrl, fixture.auth).withRequestBody(equalToJson(CheckAndDeleteRequestBody)).willReturn(
          aResponse().withStatus(BAD_REQUEST)
        ))

        whenReady(fixture.repository.deleteField(Table, RowKey, checkField = (ColumnName, CurrentValue), columnName = ColumnName)) { result =>
          result shouldBe EditFailed
        }
      }

      "when the response is a server error" in { fixture =>
        fixture.stubTargetEntityIsFound(withFields = DeleteField)
        stubHBaseFor(putHBaseJson(fixture.targetUrl, fixture.auth).withRequestBody(equalToJson(CheckAndDeleteRequestBody)).willReturn(
          aServiceUnavailableResponse()
        ))

        whenReady(fixture.repository.deleteField(Table, RowKey, checkField = (ColumnName, CurrentValue), columnName = ColumnName)) { result =>
          result shouldBe EditFailed
        }
      }
    }
  }
}
