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
import repository.hbase.PeriodTableName

trait WithWireMockHBase extends WithWireMock with BasicAuthentication with HBaseResponseFixture { this: Suite =>
  override val wireMockPort = 8075

  private val Namespace = "sbr_control_db"

  def anAllLocalUnitsForEnterpriseRequest(withErn: Ern, withPeriod: Period): MappingBuilder =
    aLocalUnitQuery(withPeriod, LocalUnitQuery.forAllWith(withErn))

  def aLocalUnitRequest(withErn: Ern, withPeriod: Period, withLurn: Lurn): MappingBuilder =
    aLocalUnitQuery(withPeriod, LocalUnitQuery.byRowKey(withErn, withLurn))

  private def aLocalUnitQuery(withPeriod: Period, withQuery: String): MappingBuilder = {
    val tableName = PeriodTableName("local_unit", withPeriod)
    createUrlAndThenGetHBaseJson(tableName, withQuery)
  }

  def anAllReportingUnitsForEnterpriseRequest(withErn: Ern, withPeriod: Period): MappingBuilder =
    aReportingUnitQuery(withPeriod, ReportingUnitQuery.forAllWith(withErn))

  def aReportingUnitRequest(withErn: Ern, withPeriod: Period, withRurn: Rurn): MappingBuilder =
    aReportingUnitQuery(withPeriod, ReportingUnitQuery.byRowKey(withErn, withRurn))

  private def aReportingUnitQuery(withPeriod: Period, withQuery: String): MappingBuilder = {
    val tableName = PeriodTableName("reporting_unit", withPeriod)
    createUrlAndThenGetHBaseJson(tableName, withQuery)
  }

  def anAllLegalUnitsForEnterpriseRequest(withErn: Ern, withPeriod: Period): MappingBuilder =
    aLegalUnitQuery(withPeriod, LegalUnitQuery.forAllWith(withErn))

  def aLegalUnitRequest(withErn: Ern, withPeriod: Period, withUbrn: Ubrn): MappingBuilder =
    aLegalUnitQuery(withPeriod, LegalUnitQuery.byRowKey(withErn, withUbrn))

  private def aLegalUnitQuery(withPeriod: Period, withQuery: String): MappingBuilder = {
    val tableName = PeriodTableName("legal_unit", withPeriod)
    createUrlAndThenGetHBaseJson(tableName, withQuery)
  }

  def anEnterpriseUnitRequest(withErn: Ern, withPeriod: Period): MappingBuilder = {
    val tableName = PeriodTableName("enterprise", withPeriod)
    createUrlAndThenGetHBaseJson(tableName, EnterpriseUnitRowKey(withErn))
  }

  def aUnitLinksExactRowKeyRequest(withStatUnit: UnitId, withUnitType: UnitType, withPeriod: Period): MappingBuilder =
    aUnitLinksQuery(query = UnitLinksRowKey(withStatUnit, withUnitType, withPeriod))

  def aUnitLinksQuery(query: String): MappingBuilder =
    createUrlAndThenGetHBaseJson(tableName = "unit_links", rowKey = query, columnFamily = UnitLinksColumnFamily)

  private def createUrlAndThenGetHBaseJson(tableName: String, rowKey: String, columnFamily: String = DefaultColumnFamily): MappingBuilder =
    getHBaseJson(
      "/" + rowKeyUrl(namespace = Namespace, table = tableName, rowKey, columnFamily = columnFamily),
      Authorization("", "")
    )

  def getHBaseJson(url: String, auth: Authorization): MappingBuilder =
    get(urlEqualTo(url)).
      withHeader("Accept", equalTo(JSON)).
      withHeader(Authorization.Name, equalTo(Authorization.value(auth)))

  def anOkResponse(): ResponseDefinitionBuilder =
    aResponse().withStatus(OK)

  val stubHBaseFor: MappingBuilder => Unit =
    WireMock.stubFor
}
