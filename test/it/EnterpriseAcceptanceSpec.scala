import java.time.Month.MARCH

import play.api.http.ContentTypes.JSON
import play.api.http.Status.OK
import org.scalatest.OptionValues

import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.{ Enterprise, Ern }

import fixture.ServerAcceptanceSpec
import it.fixture.ReadsEnterpriseUnit.enterpriseReads
import repository.hbase.unit.enterprise.EnterpriseUnitColumns._
import repository.hbase.unit.enterprise.EnterpriseUnitRowKey
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
  info("I want to retrieve a enterprise unit with a specific period (by year and month) in time")
  info("So that I can view the enterprise details via the api response")

  feature("retrieve an existing enterprise unit") {
    scenario("by exact Enterprise reference (ERN) and period") { wsClient =>
      Given(s"a enterprise rowKey of $TargetErn and $TargetPeriod")
      stubHBaseFor(aEnterpriseUnitRequest(withErn = TargetErn, withPeriod = TargetPeriod).willReturn(
        anOkResponse().withBody(EnterpriseUnitSingleMatchHBaseResponseBody)
      ))

      When(s"a enterprise unit with $TargetErn and $TargetPeriod is request")
      val response = await(wsClient.url(s"/v1/periods/${Period.asString(TargetPeriod)}/enterprises/${TargetErn.value}").get())

      Then(s"the response should match as follows and the details should resemble an expected enterprise with $TargetPeriod and $TargetErn")
      response.status shouldBe OK
      response.header("Content-Type") shouldBe Some(JSON)
      response.json.as[Enterprise] shouldBe
        Enterprise(ern = TargetErn, entref = SampleEnterpriseReference, name = SampleEnterpriseName, postcode = SamplePostcode,
          legalStatus = SampleLegalStatus, employees = Some(SampleNumberOfEmployees), jobs = Some(SampleJobs))
    }
  }

}
