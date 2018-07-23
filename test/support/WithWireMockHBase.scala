package support

import play.api.http.Status.OK
import play.mvc.Http.MimeTypes.JSON

import org.scalatest.Suite
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.client.{ MappingBuilder, ResponseDefinitionBuilder, WireMock }

import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.legalunit.Ubrn
import uk.gov.ons.sbr.models.localunit.Lurn
import uk.gov.ons.sbr.models.reportingunit.Rurn
import uk.gov.ons.sbr.models.unitlinks.{ UnitId, UnitType }

import repository.hbase.HBase.rowKeyUrl
import repository.hbase.enterprise.EnterpriseUnitRowKey
import repository.hbase.localunit.LocalUnitQuery
import repository.hbase.legalunit.LegalUnitQuery
import repository.hbase.reportingunit.ReportingUnitQuery
import repository.hbase.unitlinks.UnitLinksProperties.UnitLinksColumnFamily
import repository.hbase.unitlinks.UnitLinksRowKey
import repository.hbase.HBase.DefaultColumnFamily

trait WithWireMockHBase extends WithWireMock with BasicAuthentication with HBaseResponseFixture { this: Suite =>
  override val wireMockPort = 8075

  private val Namespace = "sbr_control_db"

  def anAllLocalUnitsForEnterpriseRequest(withErn: Ern, withPeriod: Period): MappingBuilder =
    aLocalUnitQuery(LocalUnitQuery.forAllWith(withErn, withPeriod))

  def anAllReportingUnitsForEnterpriseRequest(withErn: Ern, withPeriod: Period): MappingBuilder =
    aReportingUnitQuery(ReportingUnitQuery.forAllWith(withErn, withPeriod))

  def aLocalUnitRequest(withErn: Ern, withPeriod: Period, withLurn: Lurn): MappingBuilder =
    aLocalUnitQuery(query = LocalUnitQuery.byRowKey(withErn, withPeriod, withLurn))

  def aReportingUnitRequest(withErn: Ern, withPeriod: Period, withRurn: Rurn): MappingBuilder =
    aReportingUnitQuery(query = ReportingUnitQuery.byRowKey(withErn, withPeriod, withRurn))

  private def aReportingUnitQuery(query: String): MappingBuilder =
    createUrlAndThenGetHBaseJson(tableName = "reporting_unit", query)

  private def aLocalUnitQuery(query: String): MappingBuilder =
    createUrlAndThenGetHBaseJson(tableName = "local_unit", query)

  private def createUrlAndThenGetHBaseJson(tableName: String, rowKey: String, columnFamily: String = DefaultColumnFamily): MappingBuilder =
    getHBaseJson(
      "/" + rowKeyUrl(namespace = Namespace, table = tableName, rowKey, columnFamily = columnFamily),
      Authorization("", "")
    )

  def anAllLegalUnitsForEnterpriseRequest(withErn: Ern, withPeriod: Period): MappingBuilder =
    aLegalUnitQuery(LegalUnitQuery.forAllWith(withErn, withPeriod))

  def aLegalUnitRequest(withErn: Ern, withPeriod: Period, withUbrn: Ubrn): MappingBuilder =
    aLegalUnitQuery(query = LegalUnitQuery.byRowKey(withErn, withPeriod, withUbrn))

  private def aLegalUnitQuery(query: String): MappingBuilder =
    createUrlAndThenGetHBaseJson(tableName = "legal_unit", query)

  def aEnterpriseUnitRequest(withErn: Ern, withPeriod: Period): MappingBuilder = {
    val rowKey = EnterpriseUnitRowKey(withErn, withPeriod)
    createUrlAndThenGetHBaseJson(tableName = "enterprise", rowKey)
  }

  def aUnitLinksExactRowKeyRequest(withStatUnit: UnitId, withUnitType: UnitType, withPeriod: Period): MappingBuilder =
    aUnitLinksQuery(query = UnitLinksRowKey(withStatUnit, withUnitType, withPeriod))

  def aUnitLinksQuery(query: String): MappingBuilder =
    createUrlAndThenGetHBaseJson(tableName = "unit_links", rowKey = query, columnFamily = UnitLinksColumnFamily)

  def getHBaseJson(url: String, auth: Authorization): MappingBuilder =
    get(urlEqualTo(url)).
      withHeader("Accept", equalTo(JSON)).
      withHeader(Authorization.Name, equalTo(Authorization.value(auth)))

  def anOkResponse(): ResponseDefinitionBuilder =
    aResponse().withStatus(OK)

  val stubHBaseFor: MappingBuilder => Unit =
    WireMock.stubFor
}
