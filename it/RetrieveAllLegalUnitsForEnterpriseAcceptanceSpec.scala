import java.time.Month.MARCH

import fixture.AbstractServerAcceptanceSpec
import fixture.ReadsLegalUnit.legalUnitReads
import org.scalatest.OptionValues
import play.api.http.HeaderNames.CONTENT_TYPE
import play.api.http.Status.{BAD_REQUEST, NOT_FOUND, OK}
import play.mvc.Http.MimeTypes.JSON
import repository.hbase.HBase.DefaultColumnFamily
import repository.hbase.legalunit.LegalUnitColumns._
import repository.hbase.legalunit.LegalUnitQuery
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.legalunit.{Crn, LegalUnit, Ubrn, Uprn}
import uk.gov.ons.sbr.models.{Address, Period}

class RetrieveAllLegalUnitsForEnterpriseAcceptanceSpec extends AbstractServerAcceptanceSpec with OptionValues {

  private val TargetErn = Ern("1000000123")
  private val TargetPeriod = Period.fromYearMonth(2018, MARCH)
  private val UbrnOne = Ubrn("1111111110000000")
  private val UbrnTwo = Ubrn("0000000001111111")
  private val Family = DefaultColumnFamily

  private val LegalUnitMultipleMatchHBaseResponseBody =
    s"""{"Row": ${
      List(
        aRowWith(key = s"${LegalUnitQuery.byRowKey(TargetErn, UbrnOne)}", columns =
          aColumnWith(Family, qualifier = ubrn, value = UbrnOne.value),
          aColumnWith(Family, qualifier = name, value = "Big Box Cereal Ltd"),
          aColumnWith(Family, qualifier = tradingStyle, value = "The Cereal Co"),
          aColumnWith(Family, qualifier = address1, value = "(Rose Garden)"),
          aColumnWith(Family, qualifier = address2, value = "Glengormley"),
          aColumnWith(Family, qualifier = address3, value = "Belfast"),
          aColumnWith(Family, qualifier = postcode, value = "BT36 5HE"),
          aColumnWith(Family, qualifier = sic07, value = "10612"),
          aColumnWith(Family, qualifier = payeJobs, value = "22"),
          aColumnWith(Family, qualifier = turnover, value = "100"),
          aColumnWith(Family, qualifier = legalStatus, value = "1"),
          aColumnWith(Family, qualifier = tradingStatus, value = "A"),
          aColumnWith(Family, qualifier = birthDate, value = "03/03/2017"),
          aColumnWith(Family, qualifier = deathDate, value = "06/06/2018"),
          aColumnWith(Family, qualifier = deathCode, value = "1")
        ),
        aRowWith(key = s"${LegalUnitQuery.byRowKey(TargetErn, UbrnTwo)}", columns =
          aColumnWith(Family, qualifier = ubrn, value = UbrnTwo.value),
          aColumnWith(Family, qualifier = crn, value = "01245670"),
          aColumnWith(Family, qualifier = uprn, value = "10023450178"),
          aColumnWith(Family, qualifier = name, value = "Big Loaf Bread Ltd"),
          aColumnWith(Family, qualifier = address1, value = "1 Graves Farm"),
          aColumnWith(Family, qualifier = address2, value = "Algarkirk"),
          aColumnWith(Family, qualifier = address3, value = "Boston"),
          aColumnWith(Family, qualifier = address4, value = "Lincoln"),
          aColumnWith(Family, qualifier = address5, value = "LINCS"),
          aColumnWith(Family, qualifier = postcode, value = "PE20 2BQ"),
          aColumnWith(Family, qualifier = sic07, value = "1130"),
          aColumnWith(Family, qualifier = payeJobs, value = "16"),
          aColumnWith(Family, qualifier = turnover, value = "80"),
          aColumnWith(Family, qualifier = legalStatus, value = "1"),
          aColumnWith(Family, qualifier = tradingStatus, value = "A"),
          aColumnWith(Family, qualifier = birthDate, value = "04/04/2017")
        )
      ).mkString("[", ",", "]")
    }}"""

  info("As a SBR user")
  info("I want to retrieve all legal units for an enterprise and a period in time")
  info("So that I can view the legal unit details via the user interface")

  feature("retrieve Legal Units by exact Enterprise reference (ERN) and period") {
    scenario("when the enterprise has multiple legal units") { wsClient =>
      Given(s"two legal units exist with $TargetErn for $TargetPeriod")
      stubHBaseFor(anAllLegalUnitsForEnterpriseRequest(withErn = TargetErn, withPeriod = TargetPeriod).willReturn(
        anOkResponse().withBody(LegalUnitMultipleMatchHBaseResponseBody)
      ))

      When(s"the legal units with $TargetErn for $TargetPeriod are requested")
      val response = await(wsClient.url(s"/v1/enterprises/${TargetErn.value}/periods/${Period.asString(TargetPeriod)}/legalunits").get())

      Then(s"the details of the two legal units with $TargetErn for $TargetPeriod are returned")
      response.status shouldBe OK
      response.header(CONTENT_TYPE).value shouldBe JSON
      response.json.as[Seq[LegalUnit]] should contain theSameElementsAs Seq(
        LegalUnit(ubrn = UbrnOne, name = "Big Box Cereal Ltd", tradingStyle = Some("The Cereal Co"), sic07 = "10612",
          payeJobs = Some(22), turnover = Some(100), legalStatus = "1", tradingStatus = Some("A"),
          birthDate = "03/03/2017", deathDate = Some("06/06/2018"), deathCode = Some("1"),
          address = Address(line1 = "(Rose Garden)", line2 = Some("Glengormley"), line3 = Some("Belfast"),
            line4 = None, line5 = None, postcode = "BT36 5HE"), crn = None, uprn = None),
        LegalUnit(ubrn = UbrnTwo, name = "Big Loaf Bread Ltd", tradingStyle = None, sic07 = "1130", payeJobs = Some(16),
          turnover = Some(80), legalStatus = "1", tradingStatus = Some("A"), birthDate = "04/04/2017",
          deathDate = None, deathCode = None, address = Address(line1 = "1 Graves Farm", line2 = Some("Algarkirk"),
            line3 = Some("Boston"), line4 = Some("Lincoln"), line5 = Some("LINCS"), postcode = "PE20 2BQ"),
          crn = Some(Crn("01245670")), uprn = Some(Uprn("10023450178")))
      )
    }

    info("An existing enterprise and period in time combination should have at least one legal unit")

    scenario("when there is no such combination of Enterprise reference (ERN) and period") { wsClient =>
      Given(s"no legal units exist with $TargetErn for $TargetPeriod")
      stubHBaseFor(anAllLegalUnitsForEnterpriseRequest(withErn = TargetErn, withPeriod = TargetPeriod).willReturn(
        anOkResponse().withBody(NoMatchFoundResponse)
      ))

      When(s"the legal units with $TargetErn for $TargetPeriod are requested")
      val response = await(wsClient.url(s"/v1/enterprises/${TargetErn.value}/periods/${Period.asString(TargetPeriod)}/legalunits").get())

      Then("a NOT FOUND response is returned")
      response.status shouldBe NOT_FOUND
    }

    scenario("when the specified period is invalid") { wsClient =>
      Given(s"that a valid Period has yyyyMM format")
      val invalidPeriod = "2018-02"

      When(s"the legal units with $TargetErn for an invalid period of $invalidPeriod are requested")
      val response = await(wsClient.url(s"/v1/enterprises/${TargetErn.value}/periods/$invalidPeriod/legalunits").get())

      Then(s"a BAD REQUEST response is returned")
      response.status shouldBe BAD_REQUEST
    }
  }
}
