import java.time.Month.MARCH

import fixture.ReadsLocalUnit.localUnitReads
import fixture.ServerAcceptanceSpec
import org.scalatest.OptionValues
import play.api.http.HeaderNames.CONTENT_TYPE
import play.api.http.Status.{BAD_REQUEST, NOT_FOUND, OK}
import play.mvc.Http.MimeTypes.JSON
import repository.hbase.HBase.DefaultColumnFamily
import repository.hbase.localunit.LocalUnitColumns._
import repository.hbase.localunit.LocalUnitQuery
import support.WithWireMockHBase
import uk.gov.ons.sbr.models.enterprise.{EnterpriseLink, Ern}
import uk.gov.ons.sbr.models.localunit.{LocalUnit, Lurn}
import uk.gov.ons.sbr.models.reportingunit.{ReportingUnitLink, Rurn}
import uk.gov.ons.sbr.models.{Address, Period}

class RetrieveLocalUnitByKeyAcceptanceSpec extends ServerAcceptanceSpec with WithWireMockHBase with OptionValues {
  private val TargetErn = Ern("1000000012")
  private val TargetPeriod = Period.fromYearMonth(2018, MARCH)
  private val TargetLurn = Lurn("900000011")
  private val Family = DefaultColumnFamily

  private val LocalUnitSingleMatchHBaseResponseBody =
    s"""{"Row": ${
      List(
        aRowWith(key = s"${LocalUnitQuery.byRowKey(TargetErn, TargetLurn)}", columns =
          aColumnWith(Family, qualifier = lurn, value = TargetLurn.value),
          aColumnWith(Family, qualifier = luref, value = "some-luref"),
          aColumnWith(Family, qualifier = ern, value = TargetErn.value),
          aColumnWith(Family, qualifier = entref, value = "some-entref"),
          aColumnWith(Family, qualifier = rurn, value = "91000000012"),
          aColumnWith(Family, qualifier = ruref, value = "two-ruref"),
          aColumnWith(Family, qualifier = name, value = "some-name"),
          aColumnWith(Family, qualifier = tradingStyle, value = "some-tradingstyle"),
          aColumnWith(Family, qualifier = address1, value = "some-address1"),
          aColumnWith(Family, qualifier = address2, value = "some-address2"),
          aColumnWith(Family, qualifier = address5, value = "some-address5"),
          aColumnWith(Family, qualifier = postcode, value = "some-postcode"),
          aColumnWith(Family, qualifier = sic07, value = "some-sic07"),
          aColumnWith(Family, qualifier = employees, value = "99"),
          aColumnWith(Family, qualifier = employment, value = "100"),
          aColumnWith(Family, qualifier = region, value = "some-region"),
          aColumnWith(Family, qualifier = prn, value = "0.016587362")
        )
      ).mkString("[", ",", "]")
    }}"""

  info("As a SBR user")
  info("I want to retrieve a local unit for an enterprise and a period in time")
  info("So that I can view the local unit details via the user interface")

  feature("retrieve an existing Local Unit") {
    scenario("by exact Enterprise reference (ERN), period, and Local Unit reference (LURN)") { wsClient =>
      Given(s"a local unit exists with $TargetErn, $TargetPeriod, and $TargetLurn")
      stubHBaseFor(aLocalUnitRequest(withErn = TargetErn, withPeriod = TargetPeriod, withLurn = TargetLurn).willReturn(
        anOkResponse().withBody(LocalUnitSingleMatchHBaseResponseBody)
      ))

      When(s"the local unit with $TargetErn, $TargetPeriod, and $TargetLurn is requested")
      val response = await(wsClient.url(s"/v1/enterprises/${TargetErn.value}/periods/${Period.asString(TargetPeriod)}/localunits/${TargetLurn.value}").get())

      Then(s"the details of the unique local unit identified by $TargetErn, $TargetPeriod, and $TargetLurn are returned")
      response.status shouldBe OK
      response.header(CONTENT_TYPE).value shouldBe JSON
      response.json.as[LocalUnit] shouldBe
        LocalUnit(TargetLurn, luref = Some("some-luref"), name = "some-name", tradingStyle = Some("some-tradingstyle"),
          sic07 = "some-sic07", employees = 99, employment = 100,
          enterprise = EnterpriseLink(TargetErn, entref = Some("some-entref")),
          reportingUnit = ReportingUnitLink(Rurn("91000000012"), ruref = Some("two-ruref")),
          address = Address(line1 = "some-address1", line2 = Some("some-address2"), line3 = None, line4 = None,
            line5 = Some("some-address5"), postcode = "some-postcode"),
          region = "some-region", prn = BigDecimal("0.016587362")
        )
    }
  }

  feature("retrieve a non-existent Local Unit") {
    scenario(s"by exact Enterprise reference (ERN), period, and Local Unit reference (LURN)") { wsClient =>
      Given(s"a local unit does not exist with $TargetErn, $TargetPeriod, and $TargetLurn")
      stubHBaseFor(aLocalUnitRequest(withErn = TargetErn, withPeriod = TargetPeriod, withLurn = TargetLurn).willReturn(
        anOkResponse().withBody(NoMatchFoundResponse)
      ))

      When(s"a local unit with $TargetErn, $TargetPeriod, and $TargetLurn is requested")
      val response = await(wsClient.url(s"/v1/enterprises/${TargetErn.value}/periods/${Period.asString(TargetPeriod)}/localunits/${TargetLurn.value}").get())

      Then(s"a NOT FOUND response is returned")
      response.status shouldBe NOT_FOUND
    }
  }

  feature("validate request parameters") {
    scenario(s"rejecting an Enterprise reference (ERN) that is not ten digits long") { wsClient =>
      Given(s"that an ERN is represented by a ten digit number")

      When(s"the user requests a Local Unit having an ERN that is not ten digits long")
      val response = await(wsClient.url(s"/v1/enterprises/123456789/periods/${Period.asString(TargetPeriod)}/localunits/${TargetLurn.value}").get())

      Then(s"a BAD REQUEST response is returned")
      response.status shouldBe BAD_REQUEST
    }
  }
}
