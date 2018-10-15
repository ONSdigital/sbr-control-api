import java.time.Month.APRIL

import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import fixture.AbstractServerAcceptanceSpec
import parsers.JsonPatchBodyParser.JsonPatchMediaType
import play.api.http.HeaderNames.CONTENT_TYPE
import play.api.http.Status._
import play.mvc.Http.MimeTypes.JSON
import repository.hbase.unitlinks.{HBaseRestUnitLinksRepository, UnitLinksQualifier, UnitLinksRowKey}
import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.unitlinks.UnitId
import uk.gov.ons.sbr.models.unitlinks.UnitType.{CompaniesHouse, Enterprise, LegalUnit, PayAsYouEarn, toAcronym}

class UpdateParentLinkFromPayeUnitAcceptanceSpec extends AbstractServerAcceptanceSpec {

  private val RegisterPeriod = Period.fromYearMonth(2018, APRIL)
  private val PayeUnitAcronym = toAcronym(PayAsYouEarn)
  private val PayeRef = "125H7A71620"
  private val PayeUnitId = UnitId(PayeRef)
  private val IncorrectUBRN = "1000012345000000"
  private val TargetUBRN = "1000012345000999"
  private val TargetLegalUnitId = UnitId(TargetUBRN)
  private val Family = HBaseRestUnitLinksRepository.ColumnFamily
  private val EditedColumn = aColumnWith(Family, qualifier = "edited", value = "Y", timestamp = None)
  private val PayeUnitLinkColumns = Seq(
    aColumnWith(Family, qualifier = UnitLinksQualifier.toParent(LegalUnit), value = IncorrectUBRN)
  )

  private val UnitLinksForPayeNoClericalEditsHBaseResponseBody =
    unitLinksForPayeHBaseResponseBody(PayeUnitLinkColumns)

  private val UnitLinksForPayeWithClericalEditsHBaseResponseBody =
    unitLinksForPayeHBaseResponseBody(EditedColumn +: PayeUnitLinkColumns)

  private def unitLinksForPayeHBaseResponseBody(columns: Seq[String]): String =
    s"""{ "Row": ${
      List(
        aRowWith(key = s"${UnitLinksRowKey(PayeUnitId, PayAsYouEarn)}", columns: _*)
      ).mkString("[", ",", "]")
    }}"""

  private val UnitLinksForTargetUbrnHBaseResponseBody =
    s"""{ "Row": ${
      List(
        aRowWith(key = s"${UnitLinksRowKey(TargetLegalUnitId, LegalUnit)}", columns =
          aColumnWith(Family, qualifier = UnitLinksQualifier.toParent(Enterprise), value = "1234567890"),
          aColumnWith(Family, qualifier = UnitLinksQualifier.toChild(UnitId("87654321")), value = toAcronym(CompaniesHouse)))
      ).mkString("[", ",", "]")
    }}"""

  private val HBaseCheckAndUpdateRequestBody =
    s"""{"Row": ${
      List(
        aRowWith(key = s"${UnitLinksRowKey(PayeUnitId, PayAsYouEarn)}", columns =
          aColumnWith(Family, qualifier = "p_LEU", value = s"$TargetUBRN", timestamp = None),
          EditedColumn,
          aColumnWith(Family, qualifier = "p_LEU", value = s"$IncorrectUBRN", timestamp = None))
      ).mkString("[", ",", "]")
    }}"""

  info("As a business register subject matter expert")
  info("I want to move an incorrectly linked PAYE to a different Legal Unit")
  info("So that I can improve the quality of the business register")

  feature("update the parent Legal Unit link from a PAYE unit") {
    scenario("when the PAYE unit has no clerically edited links and the target Legal Unit already exists in the register") { wsClient =>
      Given(s"there exists a PAYE unit with reference $PayeRef")
      stubHBaseFor(aUnitLinksExactRowKeyRequest(withUnitId = PayeUnitId, withUnitType = PayAsYouEarn, withPeriod = RegisterPeriod).
        willReturn(anOkResponse().withBody(UnitLinksForPayeNoClericalEditsHBaseResponseBody)))
      And(s"there exists a Legal Unit identified by UBRN $TargetUBRN")
      stubHBaseFor(aUnitLinksExactRowKeyRequest(withUnitId = TargetLegalUnitId, withUnitType = LegalUnit, withPeriod = RegisterPeriod).
        willReturn(anOkResponse().withBody(UnitLinksForTargetUbrnHBaseResponseBody)))
      And(s"a database request to update the parent value from $IncorrectUBRN to $TargetUBRN will succeed")
      stubHBaseFor(aCheckAndUpdateUnitLinkRequest(withUnitType = PayAsYouEarn, withUnitId = PayeUnitId, withPeriod = RegisterPeriod).
        withRequestBody(equalToJson(HBaseCheckAndUpdateRequestBody)).
        willReturn(anOkResponse()))

      When(s"an update of the parent Legal Unit from $IncorrectUBRN to $TargetUBRN is requested for the PAYE unit with reference $PayeRef")
      val response = await(wsClient.url(s"/v1/periods/${Period.asString(RegisterPeriod)}/types/$PayeUnitAcronym/units/$PayeRef").
        withHeaders(CONTENT_TYPE -> JsonPatchMediaType).
        patch(s"""|[{"op": "test", "path": "/parents/LEU", "value": "$IncorrectUBRN"},
                  | {"op": "replace", "path": "/parents/LEU", "value": "$TargetUBRN"}]""".stripMargin))

      Then(s"a Success response is returned")
      response.status shouldBe NO_CONTENT
    }

    scenario("when the PAYE unit has clerically edited links and the target Legal Unit already exists in the register") { wsClient =>
      Given(s"there exists a PAYE unit with reference $PayeRef")
      stubHBaseFor(aUnitLinksExactRowKeyRequest(withUnitId = PayeUnitId, withUnitType = PayAsYouEarn, withPeriod = RegisterPeriod).
        willReturn(anOkResponse().withBody(UnitLinksForPayeWithClericalEditsHBaseResponseBody)))
      And(s"there exists a Legal Unit identified by UBRN $TargetUBRN")
      stubHBaseFor(aUnitLinksExactRowKeyRequest(withUnitId = TargetLegalUnitId, withUnitType = LegalUnit, withPeriod = RegisterPeriod).
        willReturn(anOkResponse().withBody(UnitLinksForTargetUbrnHBaseResponseBody)))
      And(s"a database request to update the parent value from $IncorrectUBRN to $TargetUBRN will succeed")
      stubHBaseFor(aCheckAndUpdateUnitLinkRequest(withUnitType = PayAsYouEarn, withUnitId = PayeUnitId, withPeriod = RegisterPeriod).
        withRequestBody(equalToJson(HBaseCheckAndUpdateRequestBody)).
        willReturn(anOkResponse()))

      When(s"an update of the parent Legal Unit from $IncorrectUBRN to $TargetUBRN is requested for the PAYE unit with reference $PayeRef")
      val response = await(wsClient.url(s"/v1/periods/${Period.asString(RegisterPeriod)}/types/$PayeUnitAcronym/units/$PayeRef").
        withHeaders(CONTENT_TYPE -> JsonPatchMediaType).
        patch(s"""|[{"op": "test", "path": "/parents/LEU", "value": "$IncorrectUBRN"},
                  | {"op": "replace", "path": "/parents/LEU", "value": "$TargetUBRN"}]""".stripMargin))

      Then(s"a Success response is returned")
      response.status shouldBe NO_CONTENT
    }

    scenario("when another user has concurrently modified the link") { wsClient =>
      Given(s"there exists a PAYE unit with reference $PayeRef")
      stubHBaseFor(aUnitLinksExactRowKeyRequest(withUnitId = PayeUnitId, withUnitType = PayAsYouEarn, withPeriod = RegisterPeriod).
        willReturn(anOkResponse().withBody(UnitLinksForPayeNoClericalEditsHBaseResponseBody)))
      And(s"there exists a Legal Unit identified by UBRN $TargetUBRN")
      stubHBaseFor(aUnitLinksExactRowKeyRequest(withUnitId = TargetLegalUnitId, withUnitType = LegalUnit, withPeriod = RegisterPeriod).
        willReturn(anOkResponse().withBody(UnitLinksForTargetUbrnHBaseResponseBody)))
      And(s"a database request to update the parent value from $IncorrectUBRN to $TargetUBRN will not succeed because of another user's change")
      stubHBaseFor(aCheckAndUpdateUnitLinkRequest(withUnitType = PayAsYouEarn, withUnitId = PayeUnitId, withPeriod = RegisterPeriod).
        withRequestBody(equalToJson(HBaseCheckAndUpdateRequestBody)).
        willReturn(aNotModifiedResponse()))

      When(s"an update of the parent Legal Unit from $IncorrectUBRN to $TargetUBRN is requested for the PAYE unit with reference $PayeRef")
      val response = await(wsClient.url(s"/v1/periods/${Period.asString(RegisterPeriod)}/types/$PayeUnitAcronym/units/$PayeRef").
        withHeaders(CONTENT_TYPE-> JsonPatchMediaType).
        patch(s"""|[{"op": "test", "path": "/parents/LEU", "value": "$IncorrectUBRN"},
                  | {"op": "replace", "path": "/parents/LEU", "value": "$TargetUBRN"}]""".stripMargin))

      Then(s"a Conflict response is returned")
      response.status shouldBe CONFLICT
    }

    scenario("when the specification of the modification does not have the Json Patch media type") { wsClient =>
      Given(s"the media type for Json Patch is $JsonPatchMediaType")

      When(s"an update of the parent Legal Unit from $IncorrectUBRN to $TargetUBRN is requested for the PAYE unit with reference $PayeRef with a media type of $JSON")
      val response = await(wsClient.url(s"/v1/periods/${Period.asString(RegisterPeriod)}/types/$PayeUnitAcronym/units/$PayeRef").
        withHeaders(CONTENT_TYPE-> JSON).
        patch(s"""|[{"op": "test", "path": "/parents/LEU", "value": "$IncorrectUBRN"},
                  | {"op": "replace", "path": "/parents/LEU", "value": "$TargetUBRN"}]""".stripMargin))

      Then(s"an Unsupported Media Type response is returned")
      response.status shouldBe UNSUPPORTED_MEDIA_TYPE
    }

    scenario("when the specification of the modification does not represent valid Json") { wsClient =>
      When(s"an update of the parent Legal Unit from $IncorrectUBRN to $TargetUBRN is requested for the PAYE unit with reference $PayeRef with invalid json")
      val invalidJson = "[}"
      val response = await(wsClient.url(s"/v1/periods/${Period.asString(RegisterPeriod)}/types/$PayeUnitAcronym/units/$PayeRef").
        withHeaders(CONTENT_TYPE-> JsonPatchMediaType).
        patch(invalidJson))

      Then(s"a Bad Request response is returned")
      response.status shouldBe BAD_REQUEST
    }

    scenario("when the specification of the modification does not comply with the Json Patch specification") { wsClient =>
      Given("that the Json Patch specification (RFC6902) does not define an 'update' operation (replace exists for this purpose)")

      When(s"an update of the parent Legal Unit from $IncorrectUBRN to $TargetUBRN is requested for the PAYE unit with reference $PayeRef")
      val invalidPatch = s"""[{"op": "update", "path": "/parents/LEU", "value": "$TargetUBRN"}]"""
      val response = await(wsClient.url(s"/v1/periods/${Period.asString(RegisterPeriod)}/types/$PayeUnitAcronym/units/$PayeRef").
        withHeaders(CONTENT_TYPE-> JsonPatchMediaType).
        patch(invalidPatch))

      Then(s"a Bad Request response is returned")
      response.status shouldBe BAD_REQUEST
    }

    scenario("when the specification of the modification is valid but inappropriate for modifying the parent link of a PAYE unit") { wsClient =>
      Given("that a patch specification containing a replace operation without a test operation is not supported by the API")

      When(s"an update of the parent Legal Unit from $IncorrectUBRN to $TargetUBRN is requested for the PAYE unit with reference $PayeRef")
      val response = await(wsClient.url(s"/v1/periods/${Period.asString(RegisterPeriod)}/types/$PayeUnitAcronym/units/$PayeRef").
        withHeaders(CONTENT_TYPE-> JsonPatchMediaType).
        patch(s"""[{"op": "replace", "path": "/parents/LEU", "value": "$TargetUBRN"}]"""))

      Then(s"a Unprocessable Entity response is returned")
      response.status shouldBe UNPROCESSABLE_ENTITY
    }

    scenario("when the target Legal Unit does not exist in the register") { wsClient =>
      Given(s"there exists a PAYE unit with reference $PayeRef")
      stubHBaseFor(aUnitLinksExactRowKeyRequest(withUnitId = PayeUnitId, withUnitType = PayAsYouEarn, withPeriod = RegisterPeriod).
        willReturn(anOkResponse().withBody(UnitLinksForPayeNoClericalEditsHBaseResponseBody)))
      And(s"there does not exist a Legal Unit identified by UBRN $TargetUBRN")
      stubHBaseFor(aUnitLinksExactRowKeyRequest(withUnitId = TargetLegalUnitId, withUnitType = LegalUnit, withPeriod = RegisterPeriod).
        willReturn(anOkResponse().withBody(NoMatchFoundResponse)))

      When(s"an update of the parent Legal Unit from $IncorrectUBRN to $TargetUBRN is requested for the PAYE unit with reference $PayeRef")
      val response = await(wsClient.url(s"/v1/periods/${Period.asString(RegisterPeriod)}/types/$PayeUnitAcronym/units/$PayeRef").
        withHeaders(CONTENT_TYPE -> JsonPatchMediaType).
        patch(s"""|[{"op": "test", "path": "/parents/LEU", "value": "$IncorrectUBRN"},
                  | {"op": "replace", "path": "/parents/LEU", "value": "$TargetUBRN"}]""".stripMargin))

      Then(s"a Unprocessable Entity response is returned")
      response.status shouldBe UNPROCESSABLE_ENTITY
    }

    scenario("when the PAYE unit being moved does not exist in the register") { wsClient =>
      Given(s"there does not exist a PAYE unit with reference $PayeRef")
      stubHBaseFor(aUnitLinksExactRowKeyRequest(withUnitId = PayeUnitId, withUnitType = PayAsYouEarn, withPeriod = RegisterPeriod).
        willReturn(anOkResponse().withBody(NoMatchFoundResponse)))
      And(s"there exists a Legal Unit identified by UBRN $TargetUBRN")
      stubHBaseFor(aUnitLinksExactRowKeyRequest(withUnitId = TargetLegalUnitId, withUnitType = LegalUnit, withPeriod = RegisterPeriod).
        willReturn(anOkResponse().withBody(UnitLinksForTargetUbrnHBaseResponseBody)))

      When(s"an update of the parent Legal Unit from $IncorrectUBRN to $TargetUBRN is requested for the PAYE unit with reference $PayeRef")
      val response = await(wsClient.url(s"/v1/periods/${Period.asString(RegisterPeriod)}/types/$PayeUnitAcronym/units/$PayeRef").
        withHeaders(CONTENT_TYPE-> JsonPatchMediaType).
        patch(s"""|[{"op": "test", "path": "/parents/LEU", "value": "$IncorrectUBRN"},
                  | {"op": "replace", "path": "/parents/LEU", "value": "$TargetUBRN"}]""".stripMargin))

      Then(s"a Not Found response is returned")
      response.status shouldBe NOT_FOUND
    }

    ignore("when the supplied PAYE reference does not adhere to the expected format") { wsClient =>
      Given("a valid PAYE reference is an alphanumeric reference between 4 and 12 characters long")

      When(s"an update of the parent Legal Unit from $IncorrectUBRN to $TargetUBRN is requested for a PAYE reference containing a non-alphanumeric character")
      val response = await(wsClient.url(s"/v1/periods/${Period.asString(RegisterPeriod)}/types/$PayeUnitAcronym/units/1234~ABCD").
        withHeaders(CONTENT_TYPE-> JsonPatchMediaType).
        patch(s"""|[{"op": "test", "path": "/parents/LEU", "value": "$IncorrectUBRN"},
                  | {"op": "replace", "path": "/parents/LEU", "value": "$TargetUBRN"}]""".stripMargin))

      Then(s"a Bad Request response is returned")
      response.status shouldBe BAD_REQUEST
    }
  }
}
