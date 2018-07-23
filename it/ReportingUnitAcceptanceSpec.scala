import java.time.Month.MARCH

import fixture.ServerAcceptanceSpec
import org.scalatest.OptionValues
import play.api.http.ContentTypes._
import play.api.http.Status.{ BAD_REQUEST, NOT_FOUND, OK }
import repository.hbase.reportingunit.ReportingUnitColumns._
import repository.hbase.reportingunit.ReportingUnitQuery
import fixture.ReadsReportingUnit.reportingUnitReads
import support.WithWireMockHBase
import support.sample.SampleReportingUnit
import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.reportingunit.{ ReportingUnit, Rurn }

class ReportingUnitAcceptanceSpec extends ServerAcceptanceSpec with WithWireMockHBase with OptionValues with SampleReportingUnit {

  private val TargetErn = Ern("1000000012")
  private val TargetPeriod = Period.fromYearMonth(2018, MARCH)
  private val TargetRurn = Rurn("33000000000")
  private val TargetRurn1 = Rurn("33000000001")

  private val ReportingUnitSingleMatchHBaseResponseBody =
    s"""{"Row": ${
      List(
        aRowWith(key = s"${ReportingUnitQuery.byRowKey(TargetErn, TargetPeriod, TargetRurn)}", columns =
          aColumnWith(name = rurn, value = TargetRurn.value),
          aColumnWith(name = ruref, value = SampleAllValuesReportingUnit.ruref.get),
          aColumnWith(name = ern, value = SampleAllValuesReportingUnit.ern.value),
          aColumnWith(name = entref, value = SampleAllValuesReportingUnit.entref.get),
          aColumnWith(name = name, value = SampleAllValuesReportingUnit.name),
          aColumnWith(name = tradingStyle, value = SampleAllValuesReportingUnit.tradingStyle.get),
          aColumnWith(name = legalStatus, value = SampleAllValuesReportingUnit.legalStatus.get),
          aColumnWith(name = address1, value = SampleAllValuesReportingUnit.address1),
          aColumnWith(name = address2, value = SampleAllValuesReportingUnit.address2.get),
          aColumnWith(name = address3, value = SampleAllValuesReportingUnit.address3.get),
          aColumnWith(name = address4, value = SampleAllValuesReportingUnit.address4.get),
          aColumnWith(name = address5, value = SampleAllValuesReportingUnit.address5.get),
          aColumnWith(name = postcode, value = SampleAllValuesReportingUnit.postcode),
          aColumnWith(name = sic07, value = SampleAllValuesReportingUnit.sic07),
          aColumnWith(name = employees, value = SampleAllValuesReportingUnit.employees.toString),
          aColumnWith(name = employment, value = SampleAllValuesReportingUnit.employment.toString),
          aColumnWith(name = turnover, value = SampleAllValuesReportingUnit.turnover.toString),
          aColumnWith(name = prn, value = SampleAllValuesReportingUnit.prn.toString()))
      ).mkString("[", ",", "]")
    }}"""

  private val ReportingUnitMultipleMatchHBaseResponseBody =
    s"""{"Row": ${
      List(
        aRowWith(key = s"${ReportingUnitQuery.byRowKey(TargetErn, TargetPeriod, TargetRurn)}", columns =
          aColumnWith(name = rurn, value = TargetRurn.value),
          aColumnWith(name = ruref, value = SampleAllValuesReportingUnit.ruref.get),
          aColumnWith(name = ern, value = SampleAllValuesReportingUnit.ern.value),
          aColumnWith(name = entref, value = SampleAllValuesReportingUnit.entref.get),
          aColumnWith(name = name, value = SampleAllValuesReportingUnit.name),
          aColumnWith(name = tradingStyle, value = SampleAllValuesReportingUnit.tradingStyle.get),
          aColumnWith(name = legalStatus, value = SampleAllValuesReportingUnit.legalStatus.get),
          aColumnWith(name = address1, value = SampleAllValuesReportingUnit.address1),
          aColumnWith(name = address2, value = SampleAllValuesReportingUnit.address2.get),
          aColumnWith(name = address3, value = SampleAllValuesReportingUnit.address3.get),
          aColumnWith(name = address4, value = SampleAllValuesReportingUnit.address4.get),
          aColumnWith(name = address5, value = SampleAllValuesReportingUnit.address5.get),
          aColumnWith(name = postcode, value = SampleAllValuesReportingUnit.postcode),
          aColumnWith(name = sic07, value = SampleAllValuesReportingUnit.sic07),
          aColumnWith(name = employees, value = SampleAllValuesReportingUnit.employees.toString),
          aColumnWith(name = employment, value = SampleAllValuesReportingUnit.employment.toString),
          aColumnWith(name = turnover, value = SampleAllValuesReportingUnit.turnover.toString),
          aColumnWith(name = prn, value = SampleAllValuesReportingUnit.prn.toString())),
        aRowWith(key = s"${ReportingUnitQuery.byRowKey(TargetErn, TargetPeriod, TargetRurn1)}", columns =
          aColumnWith(name = rurn, value = TargetRurn1.value),
          aColumnWith(name = ruref, value = SampleAllValuesReportingUnit1.ruref.get),
          aColumnWith(name = ern, value = SampleAllValuesReportingUnit1.ern.value),
          aColumnWith(name = entref, value = SampleAllValuesReportingUnit1.entref.get),
          aColumnWith(name = name, value = SampleAllValuesReportingUnit1.name),
          aColumnWith(name = tradingStyle, value = SampleAllValuesReportingUnit1.tradingStyle.get),
          aColumnWith(name = legalStatus, value = SampleAllValuesReportingUnit1.legalStatus.get),
          aColumnWith(name = address1, value = SampleAllValuesReportingUnit1.address1),
          aColumnWith(name = address2, value = SampleAllValuesReportingUnit1.address2.get),
          aColumnWith(name = address3, value = SampleAllValuesReportingUnit1.address3.get),
          aColumnWith(name = address4, value = SampleAllValuesReportingUnit1.address4.get),
          aColumnWith(name = address5, value = SampleAllValuesReportingUnit1.address5.get),
          aColumnWith(name = postcode, value = SampleAllValuesReportingUnit1.postcode),
          aColumnWith(name = sic07, value = SampleAllValuesReportingUnit1.sic07),
          aColumnWith(name = employees, value = SampleAllValuesReportingUnit1.employees.toString),
          aColumnWith(name = employment, value = SampleAllValuesReportingUnit1.employment.toString),
          aColumnWith(name = turnover, value = SampleAllValuesReportingUnit1.turnover.toString),
          aColumnWith(name = prn, value = SampleAllValuesReportingUnit1.prn.toString()))
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
      response.json.as[ReportingUnit] shouldBe SampleAllValuesReportingUnit
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
      response.json.as[Seq[ReportingUnit]] should contain theSameElementsAs Seq(SampleAllValuesReportingUnit, SampleAllValuesReportingUnit1)
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
