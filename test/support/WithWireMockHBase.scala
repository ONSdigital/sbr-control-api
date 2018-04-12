package support

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.client.{ MappingBuilder, ResponseDefinitionBuilder, WireMock }
import org.scalatest.Suite
import play.api.http.Status.OK
import play.mvc.Http.MimeTypes.JSON

import repository.hbase.HBase.rowKeyUrl
import repository.hbase.LocalUnitRowKey
import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.localunit.Lurn

import repository.hbase.unit.enterprise.EnterpriseUnitRowKey

trait WithWireMockHBase extends WithWireMock with BasicAuthentication with HBaseResponseFixture { this: Suite =>
  override val wireMockPort = 8075

  private val ColumnFamily = "d"
  private val Namespace = "sbr_control_db"

  private def createUrlAndThenGetHbaseJson(tableName: String, rowKey: String) =
    getHbaseJson(
      "/" + rowKeyUrl(namespace = Namespace, table = tableName, rowKey, columnGroup = ColumnFamily),
      Authorization("", "")
    )

  def aLocalUnitRequest(withErn: Ern, withPeriod: Period, withLurn: Lurn): MappingBuilder = {
    val rowKey = LocalUnitRowKey(withErn, withPeriod, withLurn)
    val tableName = "local_unit"
    createUrlAndThenGetHbaseJson(tableName, rowKey)
  }

  def aEnterpriseUnitRequest(withErn: Ern, withPeriod: Period): MappingBuilder = {
    val rowKey = EnterpriseUnitRowKey(withErn, withPeriod)
    val tableName = "enterprise"
    createUrlAndThenGetHbaseJson(tableName, rowKey)
  }

  def getHbaseJson(url: String, auth: Authorization): MappingBuilder =
    get(urlEqualTo(url)).
      withHeader("Accept", equalTo(JSON)).
      withHeader(Authorization.Name, equalTo(Authorization.value(auth)))

  def anOkResponse(): ResponseDefinitionBuilder =
    aResponse().withStatus(OK)

  val stubHBaseFor: MappingBuilder => Unit =
    WireMock.stubFor
}
