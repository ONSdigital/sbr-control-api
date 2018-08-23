import java.time.Month.MARCH

import fixture.ReadsEnterpriseUnit.enterpriseReads
import fixture.ServerAcceptanceSpec
import org.scalatest.OptionValues
import play.api.http.ContentTypes.JSON
import play.api.http.HeaderNames.CONTENT_TYPE
import play.api.http.Status.{BAD_REQUEST, NOT_FOUND, OK}
import repository.hbase.HBase.DefaultColumnFamily
import repository.hbase.enterprise.EnterpriseUnitColumns._
import repository.hbase.enterprise.EnterpriseUnitRowKey
import repository.hbase.localunit.LocalUnitColumns.{address1, address2, address5, postcode, sic07}
import support.WithWireMockHBase
import support.sample.SampleEnterpriseUnit
import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.{Enterprise, Ern, Turnover}

class EnterpriseAcceptanceSpec extends ServerAcceptanceSpec with WithWireMockHBase with OptionValues with SampleEnterpriseUnit {

  private val TargetErn = Ern("1000000012")
  private val TargetPeriod = Period.fromYearMonth(2018, MARCH)
  private val Family = DefaultColumnFamily

  private val EnterpriseUnitSingleMatchHBaseResponseBody =
    s"""{"Row": ${
      List(
        aRowWith(key = s"${EnterpriseUnitRowKey(TargetErn)}", columns =
          aColumnWith(Family, qualifier = ern, value = TargetErn.value),
          aColumnWith(Family, qualifier = entref, value = SampleEnterpriseReference),
          aColumnWith(Family, qualifier = name, value = SampleEnterpriseName),
          aColumnWith(Family, qualifier = tradingStyle, value = SampleTradingStyle),
          aColumnWith(Family, qualifier = address1, value = SampleAddressLine1),
          aColumnWith(Family, qualifier = address2, value = SampleAddressLine2),
          aColumnWith(Family, qualifier = address5, value = SampleAddressLine5),
          aColumnWith(Family, qualifier = postcode, value = SamplePostcode),
          aColumnWith(Family, qualifier = sic07, value = SampleSIC07),
          aColumnWith(Family, qualifier = legalStatus, value = SampleLegalStatus),
          aColumnWith(Family, qualifier = jobs, value = SampleJobs.toString),
          aColumnWith(Family, qualifier = employees, value = SampleNumberOfEmployees.toString),
          aColumnWith(Family, qualifier = containedTurnover, value = SampleContainedTurnover.toString),
          aColumnWith(Family, qualifier = standardTurnover, value = SampleStandardTurnover.toString),
          aColumnWith(Family, qualifier = groupTurnover, value = SampleGroupTurnover.toString),
          aColumnWith(Family, qualifier = enterpriseTurnover, value = SampleEnterpriseTurnover.toString),
          aColumnWith(Family, qualifier = prn, value = SamplePrn.toString()))
      ).mkString("[", ",", "]")
    }}"""

  info("As a SBR user")
  info("I want to retrieve an enterprise unit with a specific period (by year and month) in time")
  info("So that I can view the enterprise details via the api response")

  feature("retrieve an existing enterprise unit") {
    scenario("by exact Enterprise reference (ERN) and period") { wsClient =>
      Given(s"an enterprise exists with $TargetErn and $TargetPeriod")
      stubHBaseFor(anEnterpriseUnitRequest(withErn = TargetErn, withPeriod = TargetPeriod).willReturn(
        anOkResponse().withBody(EnterpriseUnitSingleMatchHBaseResponseBody)
      ))

      When(s"an enterprise unit with $TargetErn and $TargetPeriod is requested")
      val response = await(wsClient.url(s"/v1/periods/${Period.asString(TargetPeriod)}/enterprises/${TargetErn.value}").get())

      Then(s"the details of the enterprise identified by $TargetPeriod and $TargetErn should be returned")
      response.status shouldBe OK
      response.header(CONTENT_TYPE) shouldBe Some(JSON)
      response.json.as[Enterprise] shouldBe
        Enterprise(ern = TargetErn, entref = Some(SampleEnterpriseReference), name = SampleEnterpriseName,
          tradingStyle = Some(SampleTradingStyle), address = aAddressSampleWithOptionalValues(
            line2 = Some(SampleAddressLine2), line5 = Some(SampleAddressLine5)), sic07 = SampleSIC07,
          legalStatus = SampleLegalStatus, employees = Some(SampleNumberOfEmployees), jobs = Some(SampleJobs),
          turnover = Some(Turnover(containedTurnover = Some(SampleContainedTurnover),
          standardTurnover = Some(SampleStandardTurnover), groupTurnover = Some(SampleGroupTurnover),
          apportionedTurnover = None, enterpriseTurnover = Some(SampleEnterpriseTurnover))), prn = SamplePrn)
    }
  }

  feature("respond to a non-existent Enterprise Unit request") {
    scenario("by an exact Enterprise reference (ERN) and period") { wsClient =>
      Given(s"an enterprise does not exist with $TargetErn and $TargetPeriod")
      stubHBaseFor(anEnterpriseUnitRequest(withErn = TargetErn, withPeriod = TargetPeriod).willReturn(
        anOkResponse().withBody(NoMatchFoundResponse)
      ))

      When(s"an enterprise unit with $TargetErn and $TargetPeriod is requested")
      val response = await(wsClient.url(s"/v1/periods/${Period.asString(TargetPeriod)}/enterprises/${TargetErn.value}").get())

      Then("a NOT_FOUND response is returned")
      response.status shouldBe NOT_FOUND
    }
  }

  feature("responds to an invalid request") {
    scenario("rejects request due to Enterprise reference number (ERN) being too short") { wsClient =>
      Given(s"that an ERN is represented by a ten digit number")

      When(s"the user requests an enterprise having an ERN that is not ten digits long")
      val response = await(wsClient.url(s"/v1/periods/${Period.asString(TargetPeriod)}/enterprises/123456789}").get())

      Then(s"a BAD REQUEST response is returned")
      response.status shouldBe BAD_REQUEST
    }
  }
}
