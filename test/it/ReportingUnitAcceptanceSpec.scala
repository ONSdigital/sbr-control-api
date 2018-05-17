import java.time.Month.MARCH

import fixture.ServerAcceptanceSpec
import org.scalatest.OptionValues
import play.api.http.ContentTypes._
import play.api.http.Status.{ BAD_REQUEST, NOT_FOUND, OK }
import repository.hbase.reportingunit.ReportingUnitColumns._
import repository.hbase.reportingunit.ReportingUnitQuery
import fixture.ReadsReportingUnit.reportingUnitReads
import support.WithWireMockHBase
import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.reportingunit.{ ReportingUnit, Rurn }

class ReportingUnitAcceptanceSpec extends ServerAcceptanceSpec with WithWireMockHBase with OptionValues {

  private val TargetErn = Ern("1000000012")
  private val TargetPeriod = Period.fromYearMonth(2018, MARCH)
  private val TargetRurn = Rurn("33000000000")
  private val TargetRurn1 = Rurn("33000000001")

  private val ReportingUnitSingleMatchHBaseResponseBody =
    s"""{"Row": ${
      List(
        aRowWith(key = s"${ReportingUnitQuery.byRowKey(TargetErn, TargetPeriod, TargetRurn)}", columns =
          aColumnWith(name = rurn, value = TargetRurn.value),
          aColumnWith(name = ruref, value = "some-luref"))
      ).mkString("[", ",", "]")
    }}"""

  private val ReportingUnitMultipleMatchHBaseResponseBody =
    s"""{"Row": ${
      List(
        aRowWith(key = s"${ReportingUnitQuery.byRowKey(TargetErn, TargetPeriod, TargetRurn)}", columns =
          aColumnWith(name = rurn, value = TargetRurn.value),
          aColumnWith(name = ruref, value = "some-luref")),
        aRowWith(key = s"${ReportingUnitQuery.byRowKey(TargetErn, TargetPeriod, TargetRurn)}", columns =
          aColumnWith(name = rurn, value = TargetRurn1.value),
          aColumnWith(name = ruref, value = "some-luref"))
      ).mkString("[", ",", "]")
    }}"""

  info("As a SBR user")
  info("I want to retrieve a reporting unit for an enterprise and a period in time")
  info("So that I can view the reporting unit details via the user interface")

  feature("retrieve an existing Reporting Unit") {
    scenario("by exact Enterprise reference (ERN), period, and Reporting Unit reference (RURN)") { wsClient =>
      Given(s"a reporting unit exists with $TargetErn, $TargetPeriod, and $TargetRurn")
      stubHBaseFor(aReportingUnitRequest(withErn = TargetErn, withPeriod = TargetPeriod, withRurn = TargetRurn).willReturn(
        anOkResponse().withBody(ReportingUnitSingleMatchHBaseResponseBody)
      ))

      When(s"the reporting unit with $TargetErn, $TargetPeriod, and $TargetRurn is requested")
      val response = await(wsClient.url(s"/v1/enterprises/${TargetErn.value}/periods/${Period.asString(TargetPeriod)}/reportingunits/${TargetRurn.value}").get())

      Then(s"the details of the unique reporting unit identified by $TargetErn, $TargetPeriod, and $TargetRurn are returned")
      response.status shouldBe OK
      response.header("Content-Type") shouldBe Some(JSON)
      response.json.as[ReportingUnit] shouldBe ReportingUnit(TargetRurn, Some("some-luref"))
    }
  }

  feature("retrieve all Reporting Units that exist for an Enterprise") {
    scenario("by exact Enterprise reference (ERN) and period") { wsClient =>
      Given(s"multiple reporting units exist with $TargetErn, $TargetPeriod")
      stubHBaseFor(anAllReportingUnitsForEnterpriseRequest(withErn = TargetErn, withPeriod = TargetPeriod).willReturn(
        anOkResponse().withBody(ReportingUnitMultipleMatchHBaseResponseBody)
      ))

      When(s"the reporting units for $TargetErn, $TargetPeriod are requested")
      val response = await(wsClient.url(s"/v1/enterprises/${TargetErn.value}/periods/${Period.asString(TargetPeriod)}/reportingunits").get())

      Then(s"the details of all reporting units that are related to the Enterprise [$TargetErn] for period [$TargetPeriod] are returned")
      response.status shouldBe OK
      response.header("Content-Type") shouldBe Some(JSON)
      response.json.as[Seq[ReportingUnit]] should contain theSameElementsInOrderAs
        Seq(ReportingUnit(TargetRurn, Some("some-luref")), ReportingUnit(TargetRurn1, Some("some-luref")))
    }
  }

  feature("retrieve a non-existent Reporting Unit") {
    scenario(s"by exact Enterprise reference (ERN), period, and Reporting Unit reference (RURN)") { wsClient =>
      Given(s"a reporting unit does not exist with $TargetErn, $TargetPeriod, and $TargetRurn")
      stubHBaseFor(aReportingUnitRequest(withErn = TargetErn, withPeriod = TargetPeriod, withRurn = TargetRurn).willReturn(
        anOkResponse().withBody(NoMatchFoundResponse)
      ))

      When(s"a reporting unit with $TargetErn, $TargetPeriod, and $TargetRurn is requested")
      val response = await(wsClient.url(s"/v1/enterprises/${TargetErn.value}/periods/${Period.asString(TargetPeriod)}/reportingunits/${TargetRurn.value}").get())

      Then(s"a NOT FOUND response is returned")
      response.status shouldBe NOT_FOUND
    }

    scenario(s"by exact Enterprise reference (ERN) and period") { wsClient =>
      Given(s"no reporting units exist for Enterprise [$TargetErn] and period [$TargetPeriod]")
      stubHBaseFor(anAllReportingUnitsForEnterpriseRequest(withErn = TargetErn, withPeriod = TargetPeriod).willReturn(
        anOkResponse().withBody(NoMatchFoundResponse)
      ))

      When(s"the reporting units for Enterprise [$TargetErn] and period [$TargetPeriod] are requested")
      val response = await(wsClient.url(s"/v1/enterprises/${TargetErn.value}/periods/${Period.asString(TargetPeriod)}/reportingunits").get())

      Then(s"a NOT FOUND response is returned")
      response.status shouldBe NOT_FOUND
    }
  }

  feature("validate request parameters") {
    scenario(s"rejecting a Reporting Unit id (RURN) that is not eleven digits long") { wsClient =>
      Given(s"that an RURN is represented by an eleven digit number")

      When(s"the user requests a Reporting Unit having an RURN that is not eleven digits long")
      val response = await(wsClient.url(s"/v1/enterprises/${TargetErn.value}/periods/${Period.asString(TargetPeriod)}/reportingunits/${TargetRurn.value.drop(1)}").get())

      Then(s"a BAD REQUEST response is returned")
      response.status shouldBe BAD_REQUEST
    }

    scenario(s"rejecting an Enterprise reference number (ERN) that is not ten digits long") { wsClient =>
      Given(s"that an ERN is represented by a ten digit number")

      When(s"the user requests a Reporting Unit having an ERN that is not ten digits long")
      val response = await(wsClient.url(s"/v1/enterprises/${TargetErn.value.drop(1)}/periods/${Period.asString(TargetPeriod)}/reportingunits/${TargetRurn.value}").get())

      Then(s"a BAD REQUEST response is returned")
      response.status shouldBe BAD_REQUEST
    }

    scenario(s"rejecting a period that does not match the format YYYYMM") { wsClient =>
      Given(s"that a period is represented by a 6 digit number YYYYMM")

      When(s"the user requests a Reporting Unit having a period that is not 6 digits long")
      val response = await(wsClient.url(s"/v1/enterprises/${TargetErn.value}/periods/${Period.asString(TargetPeriod).drop(1)}/reportingunits/${TargetRurn.value}").get())

      Then(s"a BAD REQUEST response is returned")
      response.status shouldBe BAD_REQUEST
    }
  }
}
