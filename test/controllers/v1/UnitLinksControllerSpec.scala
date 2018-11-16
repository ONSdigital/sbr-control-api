package controllers.v1

import java.time.Month.AUGUST

import handlers.PatchHandler
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FreeSpec, Matchers, OptionValues}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import parsers.JsonPatchBodyParser.JsonPatchMediaType
import play.api.http.HeaderNames.CONTENT_TYPE
import play.api.libs.json.{JsString, Json}
import play.api.mvc.Result
import play.api.mvc.Results.NoContent
import play.api.test.Helpers._
import play.api.test.{FakeRequest, StubControllerComponentsFactory}
import play.mvc.Http.MimeTypes.JSON
import repository.UnitLinksRepository
import support.sample.SampleUnitLinks
import uk.gov.ons.sbr.models.patch.{AddOperation, ReplaceOperation, TestOperation}
import uk.gov.ons.sbr.models.unitlinks.UnitId
import uk.gov.ons.sbr.models.unitlinks.UnitType.{LegalUnit, PayAsYouEarn, ValueAddedTax, toAcronym}
import uk.gov.ons.sbr.models.{Period, UnitKey}

import scala.concurrent.Future

class UnitLinksControllerSpec extends FreeSpec with Matchers with GuiceOneAppPerSuite with MockFactory with OptionValues {

  private trait Fixture extends StubControllerComponentsFactory {
    val repository = mock[UnitLinksRepository]
    val patchHandler = mock[PatchHandler[Future[Result]]]
    val controller = new UnitLinksController(stubControllerComponents(), repository, patchHandler)
  }

  private trait ReadFixture extends Fixture with SampleUnitLinks {
    val SampleUnitLinkId = UnitKey(SampleUnitId, SampleUnitType, SamplePeriod)
  }

  private trait EditFixture extends Fixture {
    val VatRef = "862764963000"
    val PayeRef = "915H7Z74262"
    val IncorrectLegalUnitId = UnitId("1230000000000100")
    val TargetLegalUnitId = UnitId("1230000000000200")
    val SamplePeriod = Period.fromYearMonth(2018, AUGUST)
    implicit lazy val materializer = app.materializer
  }

  private trait EditParentFixture extends EditFixture {
    val VatUnitKey = UnitKey(UnitId(VatRef), ValueAddedTax, SamplePeriod)
    val PayeUnitKey = UnitKey(UnitId(PayeRef), PayAsYouEarn, SamplePeriod)
    val ExpectedPatch = Seq(
      TestOperation("/parents/LEU", JsString(IncorrectLegalUnitId.value)),
      ReplaceOperation("/parents/LEU", JsString(TargetLegalUnitId.value))
    )
    val PatchBody = s"""[{"op": "test", "path": "/parents/LEU", "value": "${IncorrectLegalUnitId.value}"},
                         {"op": "replace", "path": "/parents/LEU", "value": "${TargetLegalUnitId.value}"}]"""
    // semicolons instead of commas ...
    val InvalidJson = s"""[{"op": "test"; "path": "/parents/LEU"; "value": "${IncorrectLegalUnitId.value}"}]"""
    // missing path
    val InvalidPatch = s"""[{"op": "test", "value": "${IncorrectLegalUnitId.value}"}]"""
  }

  private trait CreateChildFixture extends EditFixture {
    val ExpectedUnitKey = UnitKey(TargetLegalUnitId, LegalUnit, SamplePeriod)
    val ExpectedPatch = Seq(AddOperation(s"/children/$VatRef", JsString(toAcronym(ValueAddedTax))))
    val PatchBody = s"""[{"op": "add", "path": "/children/$VatRef", "value": "${toAcronym(ValueAddedTax)}"}]"""
  }

  "A request" - {
    "to retrieve Unit Links by Unit Id, type and period)" - {
      "returns a JSON representation of the Unit Links when it is found" in new ReadFixture {
        (repository.retrieveUnitLinks _).expects(SampleUnitLinkId).returning(
          Future.successful(Right(Some(SampleUnitLinksWithAllFields)))
        )

        val action = controller.retrieveUnitLinks(SampleUnitId.value, Period.asString(SamplePeriod), toAcronym(SampleUnitType))
        val response = action.apply(FakeRequest())

        status(response) shouldBe OK
        contentType(response).value shouldBe JSON
        contentAsJson(response) shouldBe Json.toJson(SampleUnitLinksWithAllFields)
      }

      "returns NOT_FOUND when the Unit Links cannot be found" in new ReadFixture {
        (repository.retrieveUnitLinks _).expects(SampleUnitLinkId).returning(
          Future.successful(Right(None))
        )

        val action = controller.retrieveUnitLinks(SampleUnitId.value, Period.asString(SamplePeriod), toAcronym(SampleUnitType))
        val response = action.apply(FakeRequest())

        status(response) shouldBe NOT_FOUND
      }

      "returns GATEWAY_TIMEOUT when the retrieval time exceeds the configured time out" in new ReadFixture {
        (repository.retrieveUnitLinks _).expects(SampleUnitLinkId).returning(
          Future.successful(Left("Timeout."))
        )

        val action = controller.retrieveUnitLinks(SampleUnitId.value, Period.asString(SamplePeriod), toAcronym(SampleUnitType))
        val response = action.apply(FakeRequest())

        status(response) shouldBe GATEWAY_TIMEOUT
      }

      "returns INTERNAL_SERVER_ERROR when the retrieval fails" in new ReadFixture {
        (repository.retrieveUnitLinks _).expects(SampleUnitLinkId).returning(
          Future.successful(Left("Retrieval failed"))
        )

        val action = controller.retrieveUnitLinks(SampleUnitId.value, Period.asString(SamplePeriod), toAcronym(SampleUnitType))
        val response = action.apply(FakeRequest())

        status(response) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "to patch VAT unit links" - {
      "returns UNSUPPORTED MEDIA TYPE when the patch does not have the Json Patch Content-Type" in new EditParentFixture {
        val action = controller.patchVatUnitLinks(VatRef, Period.asString(SamplePeriod))
        val request = FakeRequest().withHeaders(CONTENT_TYPE -> JSON).withBody(PatchBody)

        val response = call(action, request)

        status(response) shouldBe UNSUPPORTED_MEDIA_TYPE
      }

      "returns BAD REQUEST when the patch is not a valid Json document" in new EditParentFixture {
        val action = controller.patchVatUnitLinks(VatRef, Period.asString(SamplePeriod))
        val request = FakeRequest().withHeaders(CONTENT_TYPE -> JsonPatchMediaType).withBody(InvalidJson)

        val response = call(action, request)

        status(response) shouldBe BAD_REQUEST
      }

      "returns BAD REQUEST when the patch does not comply with the Json Patch specification (RFC6902)" in new EditParentFixture {
        val action = controller.patchVatUnitLinks(VatRef, Period.asString(SamplePeriod))
        val request = FakeRequest().withHeaders(CONTENT_TYPE -> JsonPatchMediaType).withBody(InvalidPatch)

        val response = call(action, request)

        status(response) shouldBe BAD_REQUEST
      }

      "is handled by the patch handler when a valid Json Patch" in new EditParentFixture {
        (patchHandler.apply _).expects(VatUnitKey, ExpectedPatch).returning(
          Future.successful(NoContent)
        )
        val action = controller.patchVatUnitLinks(VatRef, Period.asString(SamplePeriod))
        val request = FakeRequest().withHeaders(CONTENT_TYPE -> JsonPatchMediaType).withBody(PatchBody)

        val response = call(action, request)

        status(response) shouldBe NO_CONTENT
      }
    }

    "to patch PAYE unit links" - {
      "returns UNSUPPORTED MEDIA TYPE when the patch does not have the Json Patch Content-Type" in new EditParentFixture {
        val action = controller.patchPayeUnitLinks(PayeRef, Period.asString(SamplePeriod))
        val request = FakeRequest().withHeaders(CONTENT_TYPE -> JSON).withBody(PatchBody)

        val response = call(action, request)

        status(response) shouldBe UNSUPPORTED_MEDIA_TYPE
      }

      "returns BAD REQUEST when the patch is not a valid Json document" in new EditParentFixture {
        val action = controller.patchPayeUnitLinks(PayeRef, Period.asString(SamplePeriod))
        val request = FakeRequest().withHeaders(CONTENT_TYPE -> JsonPatchMediaType).withBody(InvalidJson)

        val response = call(action, request)

        status(response) shouldBe BAD_REQUEST
      }

      "returns BAD REQUEST when the patch does not comply with the Json Patch specification (RFC6902)" in new EditParentFixture {
        val action = controller.patchPayeUnitLinks(PayeRef, Period.asString(SamplePeriod))
        val request = FakeRequest().withHeaders(CONTENT_TYPE -> JsonPatchMediaType).withBody(InvalidPatch)

        val response = call(action, request)

        status(response) shouldBe BAD_REQUEST
      }

      "is handled by the patch handler when a valid Json Patch" in new EditParentFixture {
        (patchHandler.apply _).expects(PayeUnitKey, ExpectedPatch).returning(
          Future.successful(NoContent)
        )
        val action = controller.patchPayeUnitLinks(PayeRef, Period.asString(SamplePeriod))
        val request = FakeRequest().withHeaders(CONTENT_TYPE -> JsonPatchMediaType).withBody(PatchBody)

        val response = call(action, request)

        status(response) shouldBe NO_CONTENT
      }
    }

    "to patch Legal Unit unit links" - {
      "returns UNSUPPORTED MEDIA TYPE when the patch does not have the Json Patch Content-Type" in new CreateChildFixture {
        val action = controller.patchLeuUnitLinks(TargetLegalUnitId.value, Period.asString(SamplePeriod))
        val request = FakeRequest().withHeaders(CONTENT_TYPE -> JSON).withBody(PatchBody)

        val response = call(action, request)

        status(response) shouldBe UNSUPPORTED_MEDIA_TYPE
      }

      "returns BAD REQUEST when the patch is not a valid Json document" in new CreateChildFixture {
        val action = controller.patchLeuUnitLinks(TargetLegalUnitId.value, Period.asString(SamplePeriod))
        // semicolons instead of commas ...
        val invalidJson = s"""[{"op": "add"; "path": "/children/$VatRef"; "value": "${toAcronym(ValueAddedTax)}"}]"""
        val request = FakeRequest().withHeaders(CONTENT_TYPE -> JsonPatchMediaType).withBody(invalidJson)

        val response = call(action, request)

        status(response) shouldBe BAD_REQUEST
      }

      "returns BAD REQUEST when the patch does not comply with the Json Patch specification (RFC6902)" in new CreateChildFixture {
        val action = controller.patchLeuUnitLinks(TargetLegalUnitId.value, Period.asString(SamplePeriod))
        val invalidPatch = s"""[{"op": "add", "path": "/children/$VatRef"]""" // missing value
        val request = FakeRequest().withHeaders(CONTENT_TYPE -> JsonPatchMediaType).withBody(invalidPatch)

        val response = call(action, request)

        status(response) shouldBe BAD_REQUEST
      }

      "is handled by the patch handler when a valid Json Patch" in new CreateChildFixture {
        (patchHandler.apply _).expects(ExpectedUnitKey, ExpectedPatch).returning(
          Future.successful(NoContent)
        )
        val action = controller.patchLeuUnitLinks(TargetLegalUnitId.value, Period.asString(SamplePeriod))
        val request = FakeRequest().withHeaders(CONTENT_TYPE -> JsonPatchMediaType).withBody(PatchBody)

        val response = call(action, request)

        status(response) shouldBe NO_CONTENT
      }
    }
  }
}
