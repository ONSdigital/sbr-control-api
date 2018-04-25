import java.time.Month.MARCH

import fixture.ServerAcceptanceSpec
import it.fixture.ReadsLocalUnit.localUnitReads
import org.scalatest.OptionValues
import play.api.http.HeaderNames.CONTENT_TYPE
import play.api.http.Status.{ BAD_REQUEST, NOT_FOUND, OK }
import play.mvc.Http.MimeTypes.JSON
import repository.hbase.localunit.LocalUnitColumns._
import repository.hbase.localunit.LocalUnitQuery
import support.WithWireMockHBase
import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.{ EnterpriseLink, Ern }
import uk.gov.ons.sbr.models.localunit.{ Address, LocalUnit, Lurn }

class RetrieveAllLocalUnitsForEnterpriseAcceptanceSpec extends ServerAcceptanceSpec with WithWireMockHBase with OptionValues {

  private val TargetErn = Ern("1000000123")
  private val TargetPeriod = Period.fromYearMonth(2018, MARCH)
  private val LurnOne = Lurn("900000010")
  private val LurnTwo = Lurn("900000020")

  private val LocalUnitMultipleMatchHBaseResponseBody =
    s"""{"Row": ${
      List(
        aRowWith(key = s"${LocalUnitQuery.byRowKey(TargetErn, TargetPeriod, LurnOne)}", columns =
          aColumnWith(name = lurn, value = LurnOne.value),
          aColumnWith(name = luref, value = s"one-luref"),
          aColumnWith(name = ern, value = TargetErn.value),
          aColumnWith(name = name, value = "one-name"),
          aColumnWith(name = address1, value = "one-address1"),
          aColumnWith(name = address2, value = "one-address2"),
          aColumnWith(name = address3, value = "one-address3"),
          aColumnWith(name = address4, value = "one-address4"),
          aColumnWith(name = postcode, value = "one-postcode"),
          aColumnWith(name = sic07, value = "one-sic07"),
          aColumnWith(name = employees, value = "42")),
        aRowWith(key = s"${LocalUnitQuery.byRowKey(TargetErn, TargetPeriod, LurnTwo)}", columns =
          aColumnWith(name = lurn, value = LurnTwo.value),
          aColumnWith(name = ern, value = TargetErn.value),
          aColumnWith(name = entref, value = "two-entref"),
          aColumnWith(name = name, value = "two-name"),
          aColumnWith(name = tradingstyle, value = "two-tradingstyle"),
          aColumnWith(name = address1, value = "two-address1"),
          aColumnWith(name = address2, value = "two-address2"),
          aColumnWith(name = address5, value = "two-address5"),
          aColumnWith(name = postcode, value = "two-postcode"),
          aColumnWith(name = sic07, value = "two-sic07"),
          aColumnWith(name = employees, value = "36"))
      ).mkString("[", ",", "]")
    }}"""

  info("As a SBR user")
  info("I want to retrieve all local units for an enterprise and a period in time")
  info("So that I can view the local unit details via the user interface")

  feature("retrieve Local Units by exact Enterprise reference (ERN) and period") {
    scenario("when the enterprise has multiple local units") { wsClient =>
      Given(s"two local units exist with $TargetErn for $TargetPeriod")
      stubHBaseFor(anAllLocalUnitsForEnterpriseRequest(withErn = TargetErn, withPeriod = TargetPeriod).willReturn(
        anOkResponse().withBody(LocalUnitMultipleMatchHBaseResponseBody)
      ))

      When(s"the local units with $TargetErn for $TargetPeriod are requested")
      val response = await(wsClient.url(s"/v1/enterprises/${TargetErn.value}/periods/${Period.asString(TargetPeriod)}/localunits").get())

      Then(s"the details of the two local units with $TargetErn for $TargetPeriod are returned")
      response.status shouldBe OK
      response.header(CONTENT_TYPE).value shouldBe JSON
      response.json.as[Seq[LocalUnit]] should contain theSameElementsAs Seq(
        LocalUnit(LurnOne, luref = Some("one-luref"), name = "one-name", tradingStyle = None,
          sic07 = "one-sic07", employees = 42, enterprise = EnterpriseLink(TargetErn, entref = None),
          address = Address(line1 = "one-address1", line2 = Some("one-address2"), line3 = Some("one-address3"),
            line4 = Some("one-address4"), line5 = None, postcode = "one-postcode")),
        LocalUnit(LurnTwo, luref = None, name = "two-name", tradingStyle = Some("two-tradingstyle"),
          sic07 = "two-sic07", employees = 36, enterprise = EnterpriseLink(TargetErn, entref = Some("two-entref")),
          address = Address(line1 = "two-address1", line2 = Some("two-address2"), line3 = None,
            line4 = None, line5 = Some("two-address5"), postcode = "two-postcode"))
      )
    }

    info("An existing enterprise and period in time combination should have at least one local unit")

    scenario("when there is no such combination of Enterprise reference (ERN) and period") { wsClient =>
      Given(s"no local units exist with $TargetErn for $TargetPeriod")
      stubHBaseFor(anAllLocalUnitsForEnterpriseRequest(withErn = TargetErn, withPeriod = TargetPeriod).willReturn(
        anOkResponse().withBody(NoMatchFoundResponse)
      ))

      When(s"the local units with $TargetErn for $TargetPeriod are requested")
      val response = await(wsClient.url(s"/v1/enterprises/${TargetErn.value}/periods/${Period.asString(TargetPeriod)}/localunits").get())

      Then("a NOT FOUND response is returned")
      response.status shouldBe NOT_FOUND
    }

    scenario("when the specified period is invalid") { wsClient =>
      Given(s"that a valid Period has yyyyMM format")
      val invalidPeriod = "2018-02"

      When(s"the local units with $TargetErn for an invalid period of $invalidPeriod are requested")
      val response = await(wsClient.url(s"/v1/enterprises/123456789/periods/$invalidPeriod/localunits").get())

      Then(s"a BAD REQUEST response is returned")
      response.status shouldBe BAD_REQUEST
    }
  }
}