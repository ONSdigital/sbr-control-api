package support

import play.api.http.Status.OK
import play.mvc.Http.MimeTypes.JSON
import org.scalatest.Suite
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.client.{ MappingBuilder, ResponseDefinitionBuilder, WireMock }

import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.localunit.Lurn
import uk.gov.ons.sbr.models.unitlinks.UnitType

import repository.hbase.HBase.{ LinksColumnFamily, UnitColumnFamily, rowKeyUrl }
import repository.hbase.enterprise.EnterpriseUnitRowKey
import repository.hbase.localunit.LocalUnitQuery
import repository.hbase.unitlinks.UnitLinksRowKey

trait WithWireMockHBase extends WithWireMock with BasicAuthentication with HBaseResponseFixture { this: Suite =>
  override val wireMockPort = 8075

  private val Namespace = "sbr_control_db"

  def anAllLocalUnitsForEnterpriseRequest(withErn: Ern, withPeriod: Period): MappingBuilder =
    aLocalUnitQuery(LocalUnitQuery.forAllWith(withErn, withPeriod))

  def aLocalUnitRequest(withErn: Ern, withPeriod: Period, withLurn: Lurn): MappingBuilder =
    aLocalUnitQuery(query = LocalUnitQuery.byRowKey(withErn, withPeriod, withLurn))

  private def aLocalUnitQuery(query: String): MappingBuilder =
    createUrlAndThenGetHBaseJson(tableName = "local_unit", query)

  private def createUrlAndThenGetHBaseJson(tableName: String, rowKey: String, columnFamily: String = UnitColumnFamily): MappingBuilder =
    getHBaseJson(
      "/" + rowKeyUrl(namespace = Namespace, table = tableName, rowKey, columnFamily = columnFamily),
      Authorization("", "")
    )

  def aEnterpriseUnitRequest(withErn: Ern, withPeriod: Period): MappingBuilder = {
    val rowKey = EnterpriseUnitRowKey(withErn, withPeriod)
    createUrlAndThenGetHBaseJson(tableName = "enterprise", rowKey)
  }

  def aUnitLinksPrefixRequest(withStatUnit: String) =
    aUnitLinksQuery(query = UnitLinksRowKey(withStatUnit))

  def aUnitLinksExactRowKeyRequest(withStatUnit: String, withUnitType: UnitType, withPeriod: Period): MappingBuilder =
    aUnitLinksQuery(query = UnitLinksRowKey(withStatUnit, withUnitType, withPeriod))

  def aUnitLinksQuery(query: String): MappingBuilder =
    createUrlAndThenGetHBaseJson(tableName = "unit_links", rowKey = query, columnFamily = LinksColumnFamily)

  def getHBaseJson(url: String, auth: Authorization): MappingBuilder =
    get(urlEqualTo(url)).
      withHeader("Accept", equalTo(JSON)).
      withHeader(Authorization.Name, equalTo(Authorization.value(auth)))

  def anOkResponse(): ResponseDefinitionBuilder =
    aResponse().withStatus(OK)

  val stubHBaseFor: MappingBuilder => Unit =
    WireMock.stubFor
}
