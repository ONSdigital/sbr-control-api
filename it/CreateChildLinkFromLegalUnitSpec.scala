import java.time.Month.MARCH

import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import fixture.ServerAcceptanceSpec
import parsers.JsonPatchBodyParser.JsonPatchMediaType
import play.api.http.HeaderNames.CONTENT_TYPE
import play.api.http.Status._
import play.mvc.Http.MimeTypes.JSON
import repository.hbase.unitlinks.{HBaseRestUnitLinksRepository, UnitLinksQualifier, UnitLinksRowKey}
import support.{WireMockAdminDataApi, WithWireMockHBase}
import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.unitlinks.UnitId
import uk.gov.ons.sbr.models.unitlinks.UnitType.{CompaniesHouse, Enterprise, LegalUnit, ValueAddedTax, toAcronym}

/*
 * For now both the fake HBase and the fake VAT admin data service share the same wiremock.
 * This implies that the VAT admin data service must be configured in application.conf to run on the wiremock port.
 * To address this, the wiremock support needs to be reworked as per sbr-api so that each fake server has an
 * independent wiremock instance.
 */
class CreateChildLinkFromLegalUnitSpec extends ServerAcceptanceSpec with WithWireMockHBase with WireMockAdminDataApi {

  private val RegisterPeriod = Period.fromYearMonth(2018, MARCH)
  private val VatUnitAcronym = toAcronym(ValueAddedTax)
  private val VatRef = "401347263289"
  private val VatUnitId = UnitId(VatRef)
  private val LegalUnitAcronym = toAcronym(LegalUnit)
  private val TargetUBRN = "1000012345000999"
  private val TargetLegalUnitId = UnitId(TargetUBRN)
  private val Family = HBaseRestUnitLinksRepository.ColumnFamily
  private val EnterpriseAcronym = toAcronym(Enterprise)
  private val EditedColumn = aColumnWith(Family, qualifier = "edited", value = "Y", timestamp = None)

  private val HBaseCreateColumnRequestBody =
    s"""{"Row": ${
      List(
        aRowWith(key = s"${UnitLinksRowKey(TargetLegalUnitId, LegalUnit)}", columns =
          aColumnWith(Family, qualifier = UnitLinksQualifier.toChild(VatUnitId), value = VatUnitAcronym, timestamp = None),
          EditedColumn
        )
      ).mkString("[", ",", "]")
    }}"""

  private def unitLinksForLegalUnitHBaseResponseBody(columns: Seq[String]): String =
    s"""{ "Row": ${
      List(
        aRowWith(key = s"${UnitLinksRowKey(TargetLegalUnitId, LegalUnit)}", columns: _*)
      ).mkString("[", ",", "]")
    }}"""

  private val UnitLinkColumns = List(
    aColumnWith(Family, qualifier = UnitLinksQualifier.toParent(Enterprise), value = "1234567890"),
    aColumnWith(Family, qualifier = UnitLinksQualifier.toChild(UnitId("87654321")), value = toAcronym(CompaniesHouse))
  )

  private val UnitLinksForLegalUnitNoClericalEditsHBaseResponseBody =
    unitLinksForLegalUnitHBaseResponseBody(UnitLinkColumns)

  private val UnitLinksForLegalUnitWithClericalEditsHBaseResponseBody =
    unitLinksForLegalUnitHBaseResponseBody(EditedColumn :: UnitLinkColumns)


  info("As a business register subject matter expert")
  info("I want to move an incorrectly linked VAT to a different Legal Unit")
  info("So that I can improve the quality of the business register")

  feature("create a link to a child VAT unit from a Legal Unit") {
    scenario("when a Legal Unit exists in the register (with no clerically edited links) and the VAT unit exists in the admin data") { wsClient =>
      Given(s"there exists a Legal Unit with UBRN $TargetUBRN and no clerical edits")
      stubHBaseFor(aUnitLinksExactRowKeyRequest(withUnitId = TargetLegalUnitId, withUnitType = LegalUnit, withPeriod = RegisterPeriod).
        willReturn(anOkResponse().withBody(UnitLinksForLegalUnitNoClericalEditsHBaseResponseBody)))
      And(s"the admin data does contain a VAT unit with reference $VatRef")
      stubAdminDataApiFor(aVatRefLookupRequest(VatUnitId, RegisterPeriod).willReturn(anOkResponse()))
      And(s"a database request to create a child link from $TargetUBRN to the VAT unit with reference $VatRef will succeed")
      stubHBaseFor(aCreateUnitLinkRequest(withUnitType = LegalUnit, withUnitId = TargetLegalUnitId, withPeriod = RegisterPeriod).
        withRequestBody(equalToJson(HBaseCreateColumnRequestBody)).
        willReturn(anOkResponse()))

      When(s"the creation of a child link from $TargetUBRN to the VAT unit with reference $VatRef is requested")
      val response = await(wsClient.url(s"/v1/periods/${Period.asString(RegisterPeriod)}/types/$LegalUnitAcronym/units/$TargetUBRN").
        withHeaders(CONTENT_TYPE -> JsonPatchMediaType).
        patch(s"""[{"op": "add", "path": "/children/$VatRef", "value": "$VatUnitAcronym"}]"""))

      Then("a Success response is returned")
      response.status shouldBe NO_CONTENT
    }

    scenario("when a Legal Unit exists in the register (with clerically edited links) and the VAT unit exists in the admin data") { wsClient =>
      Given(s"there exists a Legal Unit with UBRN $TargetUBRN and clerical edits")
      stubHBaseFor(aUnitLinksExactRowKeyRequest(withUnitId = TargetLegalUnitId, withUnitType = LegalUnit, withPeriod = RegisterPeriod).
        willReturn(anOkResponse().withBody(UnitLinksForLegalUnitWithClericalEditsHBaseResponseBody)))
      And(s"the admin data does contain a VAT unit with reference $VatRef")
      stubAdminDataApiFor(aVatRefLookupRequest(VatUnitId, RegisterPeriod).willReturn(anOkResponse()))
      And(s"a database request to create a child link from $TargetUBRN to the VAT unit with reference $VatRef will succeed")
      stubHBaseFor(aCreateUnitLinkRequest(withUnitType = LegalUnit, withUnitId = TargetLegalUnitId, withPeriod = RegisterPeriod).
        withRequestBody(equalToJson(HBaseCreateColumnRequestBody)).
        willReturn(anOkResponse()))

      When(s"the creation of a child link from $TargetUBRN to the VAT unit with reference $VatRef is requested")
      val response = await(wsClient.url(s"/v1/periods/${Period.asString(RegisterPeriod)}/types/$LegalUnitAcronym/units/$TargetUBRN").
        withHeaders(CONTENT_TYPE -> JsonPatchMediaType).
        patch(s"""[{"op": "add", "path": "/children/$VatRef", "value": "$VatUnitAcronym"}]"""))

      Then("a Success response is returned")
      response.status shouldBe NO_CONTENT
    }

    scenario("when the Legal Unit does not exist in the register") { wsClient =>
      Given(s"there does not exist a Legal Unit with UBRN $TargetUBRN")
      stubHBaseFor(aUnitLinksExactRowKeyRequest(withUnitId = TargetLegalUnitId, withUnitType = LegalUnit, withPeriod = RegisterPeriod).
        willReturn(anOkResponse().withBody(NoMatchFoundResponse)))
      And(s"the admin data does contain a VAT unit with reference $VatRef")
      stubAdminDataApiFor(aVatRefLookupRequest(VatUnitId, RegisterPeriod).willReturn(anOkResponse()))

      When(s"the creation of a child link from $TargetUBRN to the VAT unit with reference $VatRef is requested")
      val response = await(wsClient.url(s"/v1/periods/${Period.asString(RegisterPeriod)}/types/$LegalUnitAcronym/units/$TargetUBRN").
        withHeaders(CONTENT_TYPE -> JsonPatchMediaType).
        patch(s"""[{"op": "add", "path": "/children/$VatRef", "value": "$VatUnitAcronym"}]"""))

      Then(s"a Not Found response is returned")
      response.status shouldBe NOT_FOUND
    }

    scenario("when the VAT unit does not exist in the admin data") { wsClient =>
      Given(s"there exists a Legal Unit with UBRN $TargetUBRN")
      stubHBaseFor(aUnitLinksExactRowKeyRequest(withUnitId = TargetLegalUnitId, withUnitType = LegalUnit, withPeriod = RegisterPeriod).
        willReturn(anOkResponse().withBody(UnitLinksForLegalUnitNoClericalEditsHBaseResponseBody)))
      And(s"the admin data does not contain a VAT unit with reference $VatRef")
      stubAdminDataApiFor(aVatRefLookupRequest(VatUnitId, RegisterPeriod).willReturn(aNotFoundResponse()))

      When(s"the creation of a child link from $TargetUBRN to the VAT unit with reference $VatRef is requested")
      val response = await(wsClient.url(s"/v1/periods/${Period.asString(RegisterPeriod)}/types/$LegalUnitAcronym/units/$TargetUBRN").
        withHeaders(CONTENT_TYPE -> JsonPatchMediaType).
        patch(s"""[{"op": "add", "path": "/children/$VatRef", "value": "$VatUnitAcronym"}]"""))

      Then(s"a Unprocessable Entity response is returned")
      response.status shouldBe UNPROCESSABLE_ENTITY
    }

    scenario("when the supplied UBRN does not adhere to the expected format") { wsClient =>
      Given("a valid UBRN is a sixteen digit number")

      When(s"the creation of a child link from UBRN 12345678901234567 to the VAT unit with reference $VatRef is requested")
      val response = await(wsClient.url(s"/v1/periods/${Period.asString(RegisterPeriod)}/types/$LegalUnitAcronym/units/12345678901234567").
        withHeaders(CONTENT_TYPE -> JsonPatchMediaType).
        patch(s"""[{"op": "add", "path": "/children/$VatRef", "value": "$VatUnitAcronym"}]"""))

      Then(s"a Bad Request response is returned")
      response.status shouldBe BAD_REQUEST
    }

    scenario("when the specification of the modification does not have the Json Patch media type") { wsClient =>
      Given(s"the media type for Json Patch is $JsonPatchMediaType")

      When(s"the creation of a child link from $TargetUBRN to the VAT unit with reference $VatRef is requested with a media type of $JSON")
      val response = await(wsClient.url(s"/v1/periods/${Period.asString(RegisterPeriod)}/types/$LegalUnitAcronym/units/$TargetUBRN").
        withHeaders(CONTENT_TYPE -> JSON).
        patch(s"""[{"op": "add", "path": "/children/$VatRef", "value": "$VatUnitAcronym"}]"""))

      Then(s"an Unsupported Media Type response is returned")
      response.status shouldBe UNSUPPORTED_MEDIA_TYPE
    }

    scenario("when the specification of the modification does not represent valid json") { wsClient =>
      When(s"the creation of a child link from $TargetUBRN to the VAT unit with reference $VatRef is requested with invalid json")
      val invalidJson = "[}"
      val response = await(wsClient.url(s"/v1/periods/${Period.asString(RegisterPeriod)}/types/$LegalUnitAcronym/units/$TargetUBRN").
        withHeaders(CONTENT_TYPE -> JsonPatchMediaType).
        patch(invalidJson))

      Then(s"a Bad Request response is returned")
      response.status shouldBe BAD_REQUEST
    }

    scenario("when the specification of the modification does not comply with the Json Patch specification") { wsClient =>
      Given("that the Json Patch specification (RFC6902) does not define a 'create' operation (add exists for this purpose)")

      When(s"the creation of a child link from $TargetUBRN to the VAT unit with reference $VatRef is requested")
      val response = await(wsClient.url(s"/v1/periods/${Period.asString(RegisterPeriod)}/types/$LegalUnitAcronym/units/$TargetUBRN").
        withHeaders(CONTENT_TYPE -> JsonPatchMediaType).
        patch(s"""[{"op": "create", "path": "/children/$VatRef", "value": "$VatUnitAcronym"}]"""))

      Then(s"a Bad Request response is returned")
      response.status shouldBe BAD_REQUEST
    }

    scenario("when the specification of the modification is valid but inappropriate for modifying links from a Legal Unit") { wsClient =>
      Given("that a Legal Unit cannot have an enterprise as a child")

      When(s"the creation of a child link from $TargetUBRN to an Enterprise is requested")
      val response = await(wsClient.url(s"/v1/periods/${Period.asString(RegisterPeriod)}/types/$LegalUnitAcronym/units/$TargetUBRN").
        withHeaders(CONTENT_TYPE -> JsonPatchMediaType).
        patch(s"""[{"op": "add", "path": "/children/1234567890", "value": "$EnterpriseAcronym"}]"""))

      Then(s"a Unprocessable Entity response is returned")
      response.status shouldBe UNPROCESSABLE_ENTITY
    }
  }
}
