import java.time.Month.MARCH

import play.api.http.ContentTypes.JSON
import play.api.http.Status.{ OK, NOT_FOUND, BAD_REQUEST }
import org.scalatest.OptionValues

import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.{ Enterprise, Ern }

import fixture.ServerAcceptanceSpec
import it.fixture.ReadsEnterpriseUnit.enterpriseReads
import repository.hbase.enterprise.EnterpriseUnitColumns._
import repository.hbase.enterprise.EnterpriseUnitRowKey
import support.WithWireMockHBase
import support.sample.SampleEnterpriseUnit

class EnterpriseAcceptanceSpec extends ServerAcceptanceSpec with WithWireMockHBase with OptionValues with SampleEnterpriseUnit {

  private val TargetErn = Ern("1000000012")
  private val TargetPeriod = Period.fromYearMonth(2018, MARCH)

  private val EnterpriseUnitSingleMatchHBaseResponseBody =
    s"""{"Row": ${
      List(
        aRowWith(key = s"${EnterpriseUnitRowKey(TargetErn, TargetPeriod)}", columns =
          aColumnWith(name = ern, value = TargetErn.value),
          aColumnWith(name = entref, value = SampleEnterpriseReference),
          aColumnWith(name = name, value = SampleEnterpriseName),
          aColumnWith(name = postcode, value = SamplePostcode),
          aColumnWith(name = legalStatus, value = SampleLegalStatus),
          aColumnWith(name = employees, value = SampleNumberOfEmployees.toString),
          aColumnWith(name = jobs, value = SampleJobs.toString))
      ).mkString("[", ",", "]")
    }}"""

  info("As a SBR user")
  info("I want to retrieve an enterprise unit with a specific period (by year and month) in time")
  info("So that I can view the enterprise details via the api response")

  feature("retrieve an existing enterprise unit") {
    scenario("by exact Enterprise reference (ERN) and period") { wsClient =>
      Given(s"an enterprise exists with $TargetErn and $TargetPeriod")
      stubHBaseFor(aEnterpriseUnitRequest(withErn = TargetErn, withPeriod = TargetPeriod).willReturn(
        anOkResponse().withBody(EnterpriseUnitSingleMatchHBaseResponseBody)
      ))

      When(s"an enterprise unit with $TargetErn and $TargetPeriod is requested")
      val response = await(wsClient.url(s"/v1/periods/${Period.asString(TargetPeriod)}/enterprises/${TargetErn.value}").get())

      Then(s"the details of the enterprise identified by $TargetPeriod and $TargetErn should be returned")
      response.status shouldBe OK
      response.header("Content-Type") shouldBe Some(JSON)
      response.json.as[Enterprise] shouldBe
        Enterprise(ern = TargetErn, entref = SampleEnterpriseReference, name = SampleEnterpriseName, postcode = SamplePostcode,
          legalStatus = SampleLegalStatus, employees = Some(SampleNumberOfEmployees), jobs = Some(SampleJobs))
    }
  }

  feature("respond to a non-existent Enterprise Unit request") {
    scenario("by an exact Enterprise reference (ERN) and period") { wsClient =>
      Given(s"an does not exist with $TargetErn and $TargetPeriod")
      stubHBaseFor(aEnterpriseUnitRequest(withErn = TargetErn, withPeriod = TargetPeriod).willReturn(
        anOkResponse().withBody(NoMatchFoundResponse)
      ))

      When(s"an enterprise unit with $TargetErn and $TargetPeriod is requested")
      val response = await(wsClient.url(s"/v1/periods/${Period.asString(TargetPeriod)}/enterprises/${TargetErn.value}").get())

      Then("a NOT_FOUND response status to given back with no details are returned")
      response.status shouldBe NOT_FOUND
    }
  }

  feature("responds to a invalid request due to validation route validation") {
    scenario("rejects request due to Enterprise reference number (ERN) is too short") { wsClient =>
      Given(s"that an ERN is represented by a ten digit number")

      When(s"the user requests a Local Unit having an ERN that is not ten digits long")
      val response = await(wsClient.url(s"/v1/periods/${Period.asString(TargetPeriod)}/enterprises/123456789}").get())

      Then(s"a BAD REQUEST response is returned")
      response.status shouldBe BAD_REQUEST

    }
  }

}
