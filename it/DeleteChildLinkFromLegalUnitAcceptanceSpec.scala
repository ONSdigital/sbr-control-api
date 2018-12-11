import java.time.Month.MARCH

import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import fixture.AbstractServerAcceptanceSpec
import parsers.JsonPatchBodyParser.JsonPatchMediaType
import play.api.http.HeaderNames.CONTENT_TYPE
import play.api.http.Status._
import repository.hbase.unitlinks.{HBaseRestUnitLinksRepository, UnitLinksQualifier, UnitLinksRowKey}
import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.unitlinks.{UnitId, UnitType}
import uk.gov.ons.sbr.models.unitlinks.UnitType.{CompaniesHouse, Enterprise, LegalUnit, PayAsYouEarn, ValueAddedTax, toAcronym}

class DeleteChildLinkFromLegalUnitAcceptanceSpec extends AbstractServerAcceptanceSpec {

  private val RegisterPeriod = Period.fromYearMonth(2018, MARCH)
  private val TargetUBRN = "1000012345000000"
  private val TargetLegalUnitId = UnitId(TargetUBRN)
  private val LegalUnitAcronym = toAcronym(LegalUnit)
  private val VatRef = "401347263289"
  private val VatUnitId = UnitId(VatRef)
  private val VatUnitAcronym = toAcronym(ValueAddedTax)
  private val PayeRef = "125H7A71620"
  private val PayeUnitId = UnitId(PayeRef)
  private val PayeUnitAcronym = toAcronym(PayAsYouEarn)
  private val CompaniesHouseAcronym = toAcronym(CompaniesHouse)
  private val Family = HBaseRestUnitLinksRepository.ColumnFamily
  private val EditedColumn = aColumnWith(Family, qualifier = "edited", value = "Y", timestamp = None)
  private val ParentEnterprise = aColumnWith(Family, qualifier = UnitLinksQualifier.toParent(Enterprise), value = "1234567890")

  private val HBaseSetEditedFlagRequestBody =
    unitLinksForTargetUbn(EditedColumn)

  private val UnitLinksForTargetUbrnWithoutChildrenHBaseResponseBody =
    unitLinksForTargetUbn(ParentEnterprise)

  private def unitLinksForTargetUbrnWithChild(childUnitId: UnitId, childUnitType: UnitType): String =
    unitLinksForTargetUbn(
      ParentEnterprise,
      aColumnWith(Family, qualifier = UnitLinksQualifier.toChild(childUnitId), value = toAcronym(childUnitType), timestamp = None)
    )

  private def hBaseCheckAndDeleteRequestBody(childUnitId: UnitId, childUnitType: UnitType): String =
    unitLinksForTargetUbn(
      aColumnWith(Family, qualifier = UnitLinksQualifier.toChild(childUnitId), value = toAcronym(childUnitType), timestamp = None)
    )

  private def unitLinksForTargetUbn(columns: String*) =
    s"""{ "Row": ${
      List(
        aRowWith(key = s"${UnitLinksRowKey(TargetLegalUnitId, LegalUnit)}", columns: _*)
      ).mkString("[", ",", "]")
    }}"""

  info("As a business register subject matter expert")
  info("I want to move an incorrectly linked VAT or PAYE to a different Legal Unit")
  info("So that I can improve the quality of the business register")

  feature("delete a link to a child VAT unit from a Legal Unit") {
    scenario("when the link exists and can be successfully removed") { wsClient =>
      Given(s"there exists a Legal Unit identified by UBRN $TargetUBRN having a child link to the VAT unit with reference $VatRef")
      stubHBaseFor(aUnitLinksExactRowKeyRequest(withUnitId = TargetLegalUnitId, withUnitType = LegalUnit, withPeriod = RegisterPeriod).
        willReturn(anOkResponse().withBody(unitLinksForTargetUbrnWithChild(VatUnitId, ValueAddedTax))))
      And(s"a database request to delete the child link from $TargetUBRN to the VAT unit with reference $VatRef will succeed")
      stubHBaseFor(aCheckAndDeleteChildUnitLinkRequest(withUnitType = LegalUnit, withUnitId = TargetLegalUnitId, withPeriod = RegisterPeriod,
        withChildId = VatUnitId).
        withRequestBody(equalToJson(hBaseCheckAndDeleteRequestBody(VatUnitId, ValueAddedTax))).
        willReturn(anOkResponse()))
      And(s"a database request to set the clerically edited flag for the Legal Unit identified by $TargetUBRN will succeed")
      stubHBaseFor(aCreateUnitLinkRequest(withUnitType = LegalUnit, withUnitId = TargetLegalUnitId, withPeriod = RegisterPeriod).
        withRequestBody(equalToJson(HBaseSetEditedFlagRequestBody)).
        willReturn(anOkResponse()))

      When(s"the deletion of the child link from $TargetUBRN to the VAT unit with reference $VatRef is requested")
      val response = await(wsClient.url(s"/v1/periods/${Period.asString(RegisterPeriod)}/types/$LegalUnitAcronym/units/$TargetUBRN").
        withHttpHeaders(CONTENT_TYPE -> JsonPatchMediaType).
        patch(s"""|[{"op": "test", "path": "/children/$VatRef", "value": "$VatUnitAcronym"},
                  | {"op": "remove", "path": "/children/$VatRef"}]""".stripMargin))

      Then("a Success response is returned")
      response.status shouldBe NO_CONTENT
    }

    scenario("when another user has concurrently modified the link") { wsClient =>
      Given(s"there exists a Legal Unit identified by UBRN $TargetUBRN having a child link to the VAT unit with reference $VatRef")
      stubHBaseFor(aUnitLinksExactRowKeyRequest(withUnitId = TargetLegalUnitId, withUnitType = LegalUnit, withPeriod = RegisterPeriod).
        willReturn(anOkResponse().withBody(unitLinksForTargetUbrnWithChild(VatUnitId, ValueAddedTax))))
      And(s"a database request to delete the child link from $TargetUBRN to the VAT unit with reference $VatRef will not succeed because of another user's change")
      stubHBaseFor(aCheckAndDeleteChildUnitLinkRequest(withUnitType = LegalUnit, withUnitId = TargetLegalUnitId, withPeriod = RegisterPeriod,
        withChildId = VatUnitId).
        withRequestBody(equalToJson(hBaseCheckAndDeleteRequestBody(VatUnitId, ValueAddedTax))).
        willReturn(aNotModifiedResponse()))

      When(s"the deletion of the child link from $TargetUBRN to the VAT unit with reference $VatRef is requested")
      val response = await(wsClient.url(s"/v1/periods/${Period.asString(RegisterPeriod)}/types/$LegalUnitAcronym/units/$TargetUBRN").
        withHttpHeaders(CONTENT_TYPE -> JsonPatchMediaType).
        patch(s"""|[{"op": "test", "path": "/children/$VatRef", "value": "$VatUnitAcronym"},
                  | {"op": "remove", "path": "/children/$VatRef"}]""".stripMargin))

      Then("a Conflict response is returned")
      response.status shouldBe CONFLICT
    }

    /*
     * The target row exists, but the column we would like to delete does not.
     * To make our operation idempotent (and thus safe to retry) we treat this as a success case.
     *
     * Note that this was probably a mistake.  While this is the correct semantics for a delete, and we require
     * this for the sequence of actions involved in moving an admin unit to be re-runnable on failure, this specific
     * client request is a patch, and a patch should fail if a test condition is not satisfied.
     */
    scenario("when the link does not exist") { wsClient =>
      Given(s"there exists a Legal Unit identified by UBRN $TargetUBRN having no child link to the VAT unit with reference $VatRef")
      stubHBaseFor(aUnitLinksExactRowKeyRequest(withUnitId = TargetLegalUnitId, withUnitType = LegalUnit, withPeriod = RegisterPeriod).
        willReturn(anOkResponse().withBody(UnitLinksForTargetUbrnWithoutChildrenHBaseResponseBody)))
      And(s"a database request to set the clerically edited flag for the Legal Unit identified by $TargetUBRN will succeed")
      stubHBaseFor(aCreateUnitLinkRequest(withUnitType = LegalUnit, withUnitId = TargetLegalUnitId, withPeriod = RegisterPeriod).
        withRequestBody(equalToJson(HBaseSetEditedFlagRequestBody)).
        willReturn(anOkResponse()))

      When(s"the deletion of the child link from $TargetUBRN to the VAT unit with reference $VatRef is requested")
      val response = await(wsClient.url(s"/v1/periods/${Period.asString(RegisterPeriod)}/types/$LegalUnitAcronym/units/$TargetUBRN").
        withHttpHeaders(CONTENT_TYPE -> JsonPatchMediaType).
        patch(s"""|[{"op": "test", "path": "/children/$VatRef", "value": "$VatUnitAcronym"},
                  | {"op": "remove", "path": "/children/$VatRef"}]""".stripMargin))

      Then("a Success response is returned")
      response.status shouldBe NO_CONTENT
    }
  }

  feature("delete a link to a child PAYE unit from a Legal Unit") {
    scenario("when the link exists and can be successfully removed") { wsClient =>
      Given(s"there exists a Legal Unit identified by UBRN $TargetUBRN having a child link to the PAYE unit with reference $PayeRef")
      stubHBaseFor(aUnitLinksExactRowKeyRequest(withUnitId = TargetLegalUnitId, withUnitType = LegalUnit, withPeriod = RegisterPeriod).
        willReturn(anOkResponse().withBody(unitLinksForTargetUbrnWithChild(PayeUnitId, PayAsYouEarn))))
      And(s"a database request to delete the child link from $TargetUBRN to the PAYE unit with reference $PayeRef will succeed")
      stubHBaseFor(aCheckAndDeleteChildUnitLinkRequest(withUnitType = LegalUnit, withUnitId = TargetLegalUnitId, withPeriod = RegisterPeriod,
        withChildId = PayeUnitId).
        withRequestBody(equalToJson(hBaseCheckAndDeleteRequestBody(PayeUnitId, PayAsYouEarn))).
        willReturn(anOkResponse()))
      And(s"a database request to set the clerically edited flag for the Legal Unit identified by $TargetUBRN will succeed")
      stubHBaseFor(aCreateUnitLinkRequest(withUnitType = LegalUnit, withUnitId = TargetLegalUnitId, withPeriod = RegisterPeriod).
        withRequestBody(equalToJson(HBaseSetEditedFlagRequestBody)).
        willReturn(anOkResponse()))

      When(s"the deletion of the child link from $TargetUBRN to the PAYE unit with reference $PayeRef is requested")
      val response = await(wsClient.url(s"/v1/periods/${Period.asString(RegisterPeriod)}/types/$LegalUnitAcronym/units/$TargetUBRN").
        withHttpHeaders(CONTENT_TYPE -> JsonPatchMediaType).
        patch(s"""|[{"op": "test", "path": "/children/$PayeRef", "value": "$PayeUnitAcronym"},
                  | {"op": "remove", "path": "/children/$PayeRef"}]""".stripMargin))

      Then("a Success response is returned")
      response.status shouldBe NO_CONTENT
    }

    scenario("when another user has concurrently modified the link") { wsClient =>
      Given(s"there exists a Legal Unit identified by UBRN $TargetUBRN having a child link to the PAYE unit with reference $PayeRef")
      stubHBaseFor(aUnitLinksExactRowKeyRequest(withUnitId = TargetLegalUnitId, withUnitType = LegalUnit, withPeriod = RegisterPeriod).
        willReturn(anOkResponse().withBody(unitLinksForTargetUbrnWithChild(PayeUnitId, PayAsYouEarn))))
      And(s"a database request to delete the child link from $TargetUBRN to the PAYE unit with reference $PayeRef will not succeed because of another user's change")
      stubHBaseFor(aCheckAndDeleteChildUnitLinkRequest(withUnitType = LegalUnit, withUnitId = TargetLegalUnitId, withPeriod = RegisterPeriod,
        withChildId = PayeUnitId).
        withRequestBody(equalToJson(hBaseCheckAndDeleteRequestBody(PayeUnitId, PayAsYouEarn))).
        willReturn(aNotModifiedResponse()))

      When(s"the deletion of the child link from $TargetUBRN to the PAYE unit with reference $PayeRef is requested")
      val response = await(wsClient.url(s"/v1/periods/${Period.asString(RegisterPeriod)}/types/$LegalUnitAcronym/units/$TargetUBRN").
        withHttpHeaders(CONTENT_TYPE -> JsonPatchMediaType).
        patch(s"""|[{"op": "test", "path": "/children/$PayeRef", "value": "$PayeUnitAcronym"},
                  | {"op": "remove", "path": "/children/$PayeRef"}]""".stripMargin))

      Then("a Conflict response is returned")
      response.status shouldBe CONFLICT
    }

    /*
     * The target row exists, but the column we would like to delete does not.
     * To make our operation idempotent (and thus safe to retry) we treat this as a success case.
     *
     * Note that this was probably a mistake.  While this is the correct semantics for a delete, and we require
     * this for the sequence of actions involved in moving an admin unit to be re-runnable on failure, this specific
     * client request is a patch, and a patch should fail if a test condition is not satisfied.
     */
    scenario("when the link does not exist") { wsClient =>
      Given(s"there exists a Legal Unit identified by UBRN $TargetUBRN having no child link to the PAYE unit with reference $PayeRef")
      stubHBaseFor(aUnitLinksExactRowKeyRequest(withUnitId = TargetLegalUnitId, withUnitType = LegalUnit, withPeriod = RegisterPeriod).
        willReturn(anOkResponse().withBody(UnitLinksForTargetUbrnWithoutChildrenHBaseResponseBody)))
      And(s"a database request to set the clerically edited flag for the Legal Unit identified by $TargetUBRN will succeed")
      stubHBaseFor(aCreateUnitLinkRequest(withUnitType = LegalUnit, withUnitId = TargetLegalUnitId, withPeriod = RegisterPeriod).
        withRequestBody(equalToJson(HBaseSetEditedFlagRequestBody)).
        willReturn(anOkResponse()))

      When(s"the deletion of the child link from $TargetUBRN to the PAYE unit with reference $PayeRef is requested")
      val response = await(wsClient.url(s"/v1/periods/${Period.asString(RegisterPeriod)}/types/$LegalUnitAcronym/units/$TargetUBRN").
        withHttpHeaders(CONTENT_TYPE -> JsonPatchMediaType).
        patch(s"""|[{"op": "test", "path": "/children/$PayeRef", "value": "$PayeUnitAcronym"},
                  | {"op": "remove", "path": "/children/$PayeRef"}]""".stripMargin))

      Then("a Success response is returned")
      response.status shouldBe NO_CONTENT
    }
  }

  feature("common error scenarios") {
    /*
     * The target row does not exist.
     */
    scenario("when the Legal Unit does not exist in the register") { wsClient =>
      Given(s"there does not exist a Legal Unit identified by UBRN $TargetUBRN")
      stubHBaseFor(aUnitLinksExactRowKeyRequest(withUnitId = TargetLegalUnitId, withUnitType = LegalUnit, withPeriod = RegisterPeriod).
        willReturn(anOkResponse().withBody(NoMatchFoundResponse)))

      When(s"the deletion of the child link from $TargetUBRN to the VAT unit with reference $VatRef is requested")
      val response = await(wsClient.url(s"/v1/periods/${Period.asString(RegisterPeriod)}/types/$LegalUnitAcronym/units/$TargetUBRN").
        withHttpHeaders(CONTENT_TYPE -> JsonPatchMediaType).
        patch(s"""[{"op": "test", "path": "/children/$VatRef", "value": "$VatUnitAcronym"},
                 |{"op": "remove", "path": "/children/$VatRef"}]""".stripMargin))

      Then(s"a Not Found response is returned")
      response.status shouldBe NOT_FOUND
    }

    scenario("when the specification of the deletion is valid but the requested deletion is unsupported") { wsClient =>
      val childRef = VatRef
      Given(s"that deletion of a child Companies House unit is not supported")

      When(s"the deletion of the child link from $TargetUBRN to a Companies House unit is requested")
      val response = await(wsClient.url(s"/v1/periods/${Period.asString(RegisterPeriod)}/types/$LegalUnitAcronym/units/$TargetUBRN").
        withHttpHeaders(CONTENT_TYPE -> JsonPatchMediaType).
        patch(s"""[{"op": "test", "path": "/children/$childRef", "value": "$CompaniesHouseAcronym"},
                 |{"op": "remove", "path": "/children/$childRef"}]""".stripMargin))

      Then(s"a Unprocessable Entity response is returned")
      response.status shouldBe UNPROCESSABLE_ENTITY
    }

    scenario("when setting the clerically edited flag on the Legal Unit fails") { wsClient =>
      Given(s"there exists a Legal Unit identified by UBRN $TargetUBRN having a child link to the VAT unit with reference $VatRef")
      stubHBaseFor(aUnitLinksExactRowKeyRequest(withUnitId = TargetLegalUnitId, withUnitType = LegalUnit, withPeriod = RegisterPeriod).
        willReturn(anOkResponse().withBody(unitLinksForTargetUbrnWithChild(VatUnitId, ValueAddedTax))))
      And(s"a database request to delete the child link from $TargetUBRN to the VAT unit with reference $VatRef will succeed")
      stubHBaseFor(aCheckAndDeleteChildUnitLinkRequest(withUnitType = LegalUnit, withUnitId = TargetLegalUnitId, withPeriod = RegisterPeriod,
        withChildId = VatUnitId).
        withRequestBody(equalToJson(hBaseCheckAndDeleteRequestBody(VatUnitId, ValueAddedTax))).
        willReturn(anOkResponse()))
      And(s"a database request to set the clerically edited flag for the Legal Unit identified by $TargetUBRN will fail")
      stubHBaseFor(aCreateUnitLinkRequest(withUnitType = LegalUnit, withUnitId = TargetLegalUnitId, withPeriod = RegisterPeriod).
        withRequestBody(equalToJson(HBaseSetEditedFlagRequestBody)).
        willReturn(aServiceUnavailableResponse()))

      When(s"the deletion of the child link from $TargetUBRN to the VAT unit with reference $VatRef is requested")
      val response = await(wsClient.url(s"/v1/periods/${Period.asString(RegisterPeriod)}/types/$LegalUnitAcronym/units/$TargetUBRN").
        withHttpHeaders(CONTENT_TYPE -> JsonPatchMediaType).
        patch(s"""|[{"op": "test", "path": "/children/$VatRef", "value": "$VatUnitAcronym"},
                  | {"op": "remove", "path": "/children/$VatRef"}]""".stripMargin))

      Then("a failure response is returned")
      response.status shouldBe INTERNAL_SERVER_ERROR
    }
  }
}
