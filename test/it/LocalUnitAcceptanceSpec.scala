import java.time.Month.MARCH

import fixture.ServerAcceptanceSpec
import it.fixture.ReadsLocalUnit.localUnitReads
import org.scalatest.OptionValues
import play.api.http.Status.OK
import play.mvc.Http.MimeTypes.JSON
import repository.hbase.LocalUnitColumns._
import repository.hbase.LocalUnitRowKey
import support.WithWireMockHBase
import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.{ EnterpriseLink, Ern }
import uk.gov.ons.sbr.models.localunit.{ Address, LocalUnit, Lurn }

class LocalUnitAcceptanceSpec extends ServerAcceptanceSpec with WithWireMockHBase with OptionValues {
  private val TargetErn = Ern("1000000012")
  private val TargetPeriod = Period.fromYearMonth(2018, MARCH)
  private val TargetLurn = Lurn("900000011")

  private val LocalUnitSingleMatchHBaseResponseBody =
    s"""{"Row": ${
      List(
        aRowWith(key = s"${LocalUnitRowKey(TargetErn, TargetPeriod, TargetLurn)}", columns =
          aColumnWith(name = lurn, value = TargetLurn.value),
          aColumnWith(name = luref, value = "some-luref"),
          aColumnWith(name = ern, value = TargetErn.value),
          aColumnWith(name = entref, value = "some-entref"),
          aColumnWith(name = name, value = "some-name"),
          aColumnWith(name = tradingstyle, value = "some-tradingstyle"),
          aColumnWith(name = address1, value = "some-address1"),
          aColumnWith(name = address2, value = "some-address2"),
          aColumnWith(name = address5, value = "some-address5"),
          aColumnWith(name = postcode, value = "some-postcode"),
          aColumnWith(name = sic07, value = "some-sic07"),
          aColumnWith(name = employees, value = "99"))
      ).mkString("[", ",", "]")
    }}"""

  info("As a SBR user")
  info("I want to retrieve a local unit for an enterprise and a period in time")
  info("So that I can view the local unit details via the user interface")

  feature("retrieve a Local Unit") {
    scenario("by exact Enterprise reference (ERN), period, and Local Unit reference (LURN)") { wsClient =>
      Given(s"a local unit exists with $TargetErn, $TargetPeriod, and $TargetLurn")
      stubHBaseFor(aLocalUnitRequest(withErn = TargetErn, withPeriod = TargetPeriod, withLurn = TargetLurn).willReturn(
        anOkResponse().withBody(LocalUnitSingleMatchHBaseResponseBody)
      ))

      When(s"the local unit with $TargetErn, $TargetPeriod, and $TargetLurn is requested")
      val response = await(wsClient.url(s"/v1/enterprises/${TargetErn.value}/periods/${Period.asString(TargetPeriod)}/localunits/${TargetLurn.value}").get())

      Then(s"the details of the unique local unit identified by $TargetErn, $TargetPeriod, and $TargetLurn are returned")
      response.status shouldBe OK
      response.header("Content-Type").value shouldBe JSON
      response.json.as[LocalUnit] shouldBe
        LocalUnit(TargetLurn, luref = "some-luref", name = "some-name", tradingStyle = "some-tradingstyle",
          sic07 = "some-sic07", employees = 99, enterprise = EnterpriseLink(TargetErn, entref = Some("some-entref")),
          address = Address(line1 = "some-address1", line2 = Some("some-address2"), line3 = None, line4 = None,
            line5 = Some("some-address5"), postcode = "some-postcode"))
    }
  }
}
