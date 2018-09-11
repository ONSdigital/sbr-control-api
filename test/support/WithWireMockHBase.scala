package support

import play.api.http.Status.{ NOT_MODIFIED, OK, SERVICE_UNAVAILABLE }
import play.mvc.Http.MimeTypes.JSON
import org.scalatest.Suite
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.client.{ MappingBuilder, ResponseDefinitionBuilder, WireMock }
import play.api.http.HeaderNames.{ ACCEPT, CONTENT_TYPE }
import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.legalunit.Ubrn
import uk.gov.ons.sbr.models.localunit.Lurn
import uk.gov.ons.sbr.models.reportingunit.Rurn
import uk.gov.ons.sbr.models.unitlinks.{ UnitId, UnitType }
import repository.hbase.HBase.{ checkedPutUrl, rowKeyUrl }
import repository.hbase.enterprise.EnterpriseUnitRowKey
import repository.hbase.localunit.LocalUnitQuery
import repository.hbase.legalunit.LegalUnitQuery
import repository.hbase.reportingunit.ReportingUnitQuery
import repository.hbase.unitlinks.{ HBaseRestUnitLinksRepository, UnitLinksRowKey }
import repository.hbase.HBase.DefaultColumnFamily
import repository.hbase.PeriodTableName

trait WithWireMockHBase extends WithWireMock with BasicAuthentication with HBaseJsonBodyFixture { this: Suite =>
  override val wireMockPort = 8075

  private val Namespace = "sbr_control_db"

  def anAllLocalUnitsForEnterpriseRequest(withErn: Ern, withPeriod: Period): MappingBuilder =
    aLocalUnitQuery(withPeriod, LocalUnitQuery.forAllWith(withErn))

  def aLocalUnitRequest(withErn: Ern, withPeriod: Period, withLurn: Lurn): MappingBuilder =
    aLocalUnitQuery(withPeriod, LocalUnitQuery.byRowKey(withErn, withLurn))

  private def aLocalUnitQuery(withPeriod: Period, withQuery: String): MappingBuilder = {
    val tableName = PeriodTableName("local_unit", withPeriod)
    getHBaseJson(url = urlFor(tableName, withQuery, DefaultColumnFamily), auth = dummyAuthorization)
  }

  def anAllReportingUnitsForEnterpriseRequest(withErn: Ern, withPeriod: Period): MappingBuilder =
    aReportingUnitQuery(withPeriod, ReportingUnitQuery.forAllWith(withErn))

  def aReportingUnitRequest(withErn: Ern, withPeriod: Period, withRurn: Rurn): MappingBuilder =
    aReportingUnitQuery(withPeriod, ReportingUnitQuery.byRowKey(withErn, withRurn))

  private def aReportingUnitQuery(withPeriod: Period, withQuery: String): MappingBuilder = {
    val tableName = PeriodTableName("reporting_unit", withPeriod)
    getHBaseJson(url = urlFor(tableName, withQuery, DefaultColumnFamily), auth = dummyAuthorization)
  }

  def anAllLegalUnitsForEnterpriseRequest(withErn: Ern, withPeriod: Period): MappingBuilder =
    aLegalUnitQuery(withPeriod, LegalUnitQuery.forAllWith(withErn))

  def aLegalUnitRequest(withErn: Ern, withPeriod: Period, withUbrn: Ubrn): MappingBuilder =
    aLegalUnitQuery(withPeriod, LegalUnitQuery.byRowKey(withErn, withUbrn))

  private def aLegalUnitQuery(withPeriod: Period, withQuery: String): MappingBuilder = {
    val tableName = PeriodTableName("legal_unit", withPeriod)
    getHBaseJson(url = urlFor(tableName, withQuery, DefaultColumnFamily), auth = dummyAuthorization)
  }

  def anEnterpriseUnitRequest(withErn: Ern, withPeriod: Period): MappingBuilder = {
    val tableName = PeriodTableName("enterprise", withPeriod)
    getHBaseJson(url = urlFor(tableName, EnterpriseUnitRowKey(withErn), DefaultColumnFamily), auth = dummyAuthorization)
  }

  def aUnitLinksExactRowKeyRequest(withUnitId: UnitId, withUnitType: UnitType, withPeriod: Period): MappingBuilder = {
    val tableName = PeriodTableName("unit_link", withPeriod)
    val rowKey = UnitLinksRowKey(withUnitId, withUnitType)
    getHBaseJson(
      url = urlFor(tableName, rowKey, columnFamily = HBaseRestUnitLinksRepository.ColumnFamily),
      auth = dummyAuthorization
    )
  }

  def aCheckAndUpdateUnitLinkRequest(withUnitType: UnitType, withUnitId: UnitId, withPeriod: Period): MappingBuilder = {
    val tableName = PeriodTableName("unit_link", withPeriod)
    val rowKey = UnitLinksRowKey(withUnitId, withUnitType)
    putHBaseJson(url = urlForCheckedPut(tableName, rowKey), auth = dummyAuthorization)
  }

  def getHBaseJson(url: String, auth: Authorization): MappingBuilder =
    get(urlEqualTo(url)).
      withHeader(ACCEPT, equalTo(JSON)).
      withHeader(Authorization.Name, equalTo(Authorization.value(auth)))

  def putHBaseJson(url: String, auth: Authorization): MappingBuilder =
    put(urlEqualTo(url)).
      withHeader(CONTENT_TYPE, equalTo(JSON)).
      withHeader(Authorization.Name, equalTo(Authorization.value(auth)))

  private def urlFor(tableName: String, rowKey: String, columnFamily: String): String =
    "/" + rowKeyUrl(namespace = Namespace, tableName, rowKey, columnFamily)

  private def urlForCheckedPut(tableName: String, rowKey: String): String =
    "/" + checkedPutUrl(namespace = Namespace, tableName, rowKey)

  def anOkResponse(): ResponseDefinitionBuilder =
    aResponse().withStatus(OK)

  def aNotModifiedResponse(): ResponseDefinitionBuilder =
    aResponse().withStatus(NOT_MODIFIED)

  def aServiceUnavailableResponse(): ResponseDefinitionBuilder =
    aResponse().withStatus(SERVICE_UNAVAILABLE)

  val stubHBaseFor: MappingBuilder => Unit =
    WireMock.stubFor

  private val dummyAuthorization: Authorization =
    Authorization("", "")
}
