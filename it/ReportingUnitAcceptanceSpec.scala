import java.time.Month.MARCH

import fixture.AbstractServerAcceptanceSpec
import fixture.ReadsReportingUnit.reportingUnitReads
import org.scalatest.OptionValues
import play.api.http.ContentTypes._
import play.api.http.HeaderNames.CONTENT_TYPE
import play.api.http.Status.{BAD_REQUEST, NOT_FOUND, OK}
import repository.hbase.HBase.DefaultColumnFamily
import repository.hbase.reportingunit.ReportingUnitColumns._
import repository.hbase.reportingunit.ReportingUnitQuery
import support.sample.SampleReportingUnit
import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.reportingunit.{ReportingUnit, Rurn}

class ReportingUnitAcceptanceSpec extends AbstractServerAcceptanceSpec with OptionValues with SampleReportingUnit {

  private val TargetErn = Ern("1000000012")
  private val TargetPeriod = Period.fromYearMonth(2018, MARCH)
  private val TargetRurn = Rurn("33000000000")
  private val TargetRurn1 = Rurn("33000000001")
  private val Family = DefaultColumnFamily

  private val ReportingUnitSingleMatchHBaseResponseBody =
    s"""{"Row": ${
      List(
        aRowWith(key = s"${ReportingUnitQuery.byRowKey(TargetErn, TargetRurn)}", columns =
          aColumnWith(Family, qualifier = rurn, value = TargetRurn.value),
          aColumnWith(Family, qualifier = ruref, value = SampleAllValuesReportingUnit.ruref.get),
          aColumnWith(Family, qualifier = ern, value = SampleEnterpriseLink.ern.value),
          aColumnWith(Family, qualifier = entref, value = SampleEnterpriseLink.entref.get),
          aColumnWith(Family, qualifier = name, value = SampleAllValuesReportingUnit.name),
          aColumnWith(Family, qualifier = tradingStyle, value = SampleAllValuesReportingUnit.tradingStyle.get),
          aColumnWith(Family, qualifier = legalStatus, value = SampleAllValuesReportingUnit.legalStatus),
          aColumnWith(Family, qualifier = address1, value = SampleAddress.line1),
          aColumnWith(Family, qualifier = address2, value = SampleAddress.line2.get),
          aColumnWith(Family, qualifier = address3, value = SampleAddress.line3.get),
          aColumnWith(Family, qualifier = address4, value = SampleAddress.line4.get),
          aColumnWith(Family, qualifier = address5, value = SampleAddress.line5.get),
          aColumnWith(Family, qualifier = postcode, value = SampleAddress.postcode),
          aColumnWith(Family, qualifier = sic07, value = SampleAllValuesReportingUnit.sic07),
          aColumnWith(Family, qualifier = employees, value = SampleAllValuesReportingUnit.employees.toString),
          aColumnWith(Family, qualifier = employment, value = SampleAllValuesReportingUnit.employment.toString),
          aColumnWith(Family, qualifier = turnover, value = SampleAllValuesReportingUnit.turnover.toString),
          aColumnWith(Family, qualifier = prn, value = SampleAllValuesReportingUnit.prn.toString()),
          aColumnWith(Family, qualifier = region, value = SampleAllValuesReportingUnit.region)
        )
      ).mkString("[", ",", "]")
    }}"""

  private val ReportingUnitMultipleMatchHBaseResponseBody =
    s"""{"Row": ${
      List(
        aRowWith(key = s"${ReportingUnitQuery.byRowKey(TargetErn, TargetRurn)}", columns =
          aColumnWith(Family, qualifier = rurn, value = TargetRurn.value),
          aColumnWith(Family, qualifier = ruref, value = SampleAllValuesReportingUnit.ruref.get),
          aColumnWith(Family, qualifier = ern, value = SampleEnterpriseLink.ern.value),
          aColumnWith(Family, qualifier = entref, value = SampleEnterpriseLink.entref.get),
          aColumnWith(Family, qualifier = name, value = SampleAllValuesReportingUnit.name),
          aColumnWith(Family, qualifier = tradingStyle, value = SampleAllValuesReportingUnit.tradingStyle.get),
          aColumnWith(Family, qualifier = legalStatus, value = SampleAllValuesReportingUnit.legalStatus),
          aColumnWith(Family, qualifier = address1, value = SampleAddress.line1),
          aColumnWith(Family, qualifier = address2, value = SampleAddress.line2.get),
          aColumnWith(Family, qualifier = address3, value = SampleAddress.line3.get),
          aColumnWith(Family, qualifier = address4, value = SampleAddress.line4.get),
          aColumnWith(Family, qualifier = address5, value = SampleAddress.line5.get),
          aColumnWith(Family, qualifier = postcode, value = SampleAddress.postcode),
          aColumnWith(Family, qualifier = sic07, value = SampleAllValuesReportingUnit.sic07),
          aColumnWith(Family, qualifier = employees, value = SampleAllValuesReportingUnit.employees.toString),
          aColumnWith(Family, qualifier = employment, value = SampleAllValuesReportingUnit.employment.toString),
          aColumnWith(Family, qualifier = turnover, value = SampleAllValuesReportingUnit.turnover.toString),
          aColumnWith(Family, qualifier = prn, value = SampleAllValuesReportingUnit.prn.toString()),
          aColumnWith(Family, qualifier = region, value = SampleAllValuesReportingUnit.region)),
        aRowWith(key = s"${ReportingUnitQuery.byRowKey(TargetErn, TargetRurn1)}", columns =
          aColumnWith(Family, qualifier = rurn, value = TargetRurn1.value),
          aColumnWith(Family, qualifier = ruref, value = SampleAllValuesReportingUnit1.ruref.get),
          aColumnWith(Family, qualifier = ern, value = SampleEnterpriseLink.ern.value),
          aColumnWith(Family, qualifier = entref, value = SampleEnterpriseLink.entref.get),
          aColumnWith(Family, qualifier = name, value = SampleAllValuesReportingUnit1.name),
          aColumnWith(Family, qualifier = tradingStyle, value = SampleAllValuesReportingUnit1.tradingStyle.get),
          aColumnWith(Family, qualifier = legalStatus, value = SampleAllValuesReportingUnit1.legalStatus),
          aColumnWith(Family, qualifier = address1, value = SampleAddress1.line1),
          aColumnWith(Family, qualifier = address2, value = SampleAddress1.line2.get),
          aColumnWith(Family, qualifier = address3, value = SampleAddress1.line3.get),
          aColumnWith(Family, qualifier = address4, value = SampleAddress1.line4.get),
          aColumnWith(Family, qualifier = address5, value = SampleAddress1.line5.get),
          aColumnWith(Family, qualifier = postcode, value = SampleAddress1.postcode),
          aColumnWith(Family, qualifier = sic07, value = SampleAllValuesReportingUnit1.sic07),
          aColumnWith(Family, qualifier = employees, value = SampleAllValuesReportingUnit1.employees.toString),
          aColumnWith(Family, qualifier = employment, value = SampleAllValuesReportingUnit1.employment.toString),
          aColumnWith(Family, qualifier = turnover, value = SampleAllValuesReportingUnit1.turnover.toString),
          aColumnWith(Family, qualifier = prn, value = SampleAllValuesReportingUnit1.prn.toString()),
          aColumnWith(Family, qualifier = region, value = SampleAllValuesReportingUnit1.region))
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
      response.header(CONTENT_TYPE) shouldBe Some(JSON)
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
      response.header(CONTENT_TYPE) shouldBe Some(JSON)
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
