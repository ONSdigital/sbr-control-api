package controllers.v1

import java.time.Month.AUGUST

import org.scalamock.scalatest.MockFactory
import org.scalatest.{ FreeSpec, Matchers, OptionValues }
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import parsers.JsonPatchBodyParser.JsonPatchMediaType
import play.api.http.HeaderNames.CONTENT_TYPE
import play.api.libs.json.{ JsString, Json }
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.mvc.Http.MimeTypes.JSON
import repository.UnitLinksRepository
import services._
import support.sample.SampleUnitLinks
import uk.gov.ons.sbr.models.patch.Operation
import uk.gov.ons.sbr.models.patch.OperationTypes.{ Replace, Test }
import uk.gov.ons.sbr.models.unitlinks.{ UnitId, UnitType }
import uk.gov.ons.sbr.models.{ Period, UnitKey }

import scala.concurrent.Future

class UnitLinksControllerSpec extends FreeSpec with Matchers with GuiceOneAppPerSuite with MockFactory with OptionValues {

  private trait Fixture {
    val repository = mock[UnitLinksRepository]
    val patchService = mock[PatchService]
    val controller = new UnitLinksController(repository, patchService)
  }

  private trait ReadFixture extends Fixture with SampleUnitLinks {
    val SampleUnitLinkId = UnitKey(SampleUnitId, SampleUnitType, SamplePeriod)
  }

  private trait EditFixture extends Fixture {
    val VatRef = "862764963000"
    val IncorrectLegalUnitId = UnitId("1230000000000100")
    val TargetLegalUnitId = UnitId("1230000000000200")
    val SamplePeriod = Period.fromYearMonth(2018, AUGUST)
    val ExpectedUnitKey = UnitKey(UnitId(VatRef), UnitType.ValueAddedTax, SamplePeriod)
    val ExpectedPatch = Seq(
      Operation(Test, "/parents/LEU", JsString(IncorrectLegalUnitId.value)),
      Operation(Replace, "/parents/LEU", JsString(TargetLegalUnitId.value))
    )
    val PatchBody = s"""[{"op": "test", "path": "/parents/LEU", "value": "${IncorrectLegalUnitId.value}"},
                         {"op": "replace", "path": "/parents/LEU", "value": "${TargetLegalUnitId.value}"}]"""

    implicit lazy val materializer = app.materializer
  }

  "A request" - {
    "to retrieve Unit Links by Unit Id, type and period)" - {
      "returns a JSON representation of the Unit Links when it is found" in new ReadFixture {
        (repository.retrieveUnitLinks _).expects(SampleUnitLinkId).returning(
          Future.successful(Right(Some(SampleUnitLinksWithAllFields)))
        )

        val action = controller.retrieveUnitLinks(SampleUnitId.value, Period.asString(SamplePeriod), UnitType.toAcronym(SampleUnitType))
        val response = action.apply(FakeRequest())

        status(response) shouldBe OK
        contentType(response).value shouldBe JSON
        contentAsJson(response) shouldBe Json.toJson(SampleUnitLinksWithAllFields)
      }

      "returns NOT_FOUND when the Unit Links cannot be found" in new ReadFixture {
        (repository.retrieveUnitLinks _).expects(SampleUnitLinkId).returning(
          Future.successful(Right(None))
        )

        val action = controller.retrieveUnitLinks(SampleUnitId.value, Period.asString(SamplePeriod), UnitType.toAcronym(SampleUnitType))
        val response = action.apply(FakeRequest())

        status(response) shouldBe NOT_FOUND
      }

      "returns GATEWAY_TIMEOUT when the retrieval time exceeds the configured time out" in new ReadFixture {
        (repository.retrieveUnitLinks _).expects(SampleUnitLinkId).returning(
          Future.successful(Left("Timeout."))
        )

        val action = controller.retrieveUnitLinks(SampleUnitId.value, Period.asString(SamplePeriod), UnitType.toAcronym(SampleUnitType))
        val response = action.apply(FakeRequest())

        status(response) shouldBe GATEWAY_TIMEOUT
      }

      "returns INTERNAL_SERVER_ERROR when the retrieval fails" in new ReadFixture {
        (repository.retrieveUnitLinks _).expects(SampleUnitLinkId).returning(
          Future.successful(Left("Retrieval failed"))
        )

        val action = controller.retrieveUnitLinks(SampleUnitId.value, Period.asString(SamplePeriod), UnitType.toAcronym(SampleUnitType))
        val response = action.apply(FakeRequest())

        status(response) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "to patch VAT unit links" - {
      "succeeds when the repository is updated successfully" in new EditFixture {
        (patchService.applyPatchTo _).expects(ExpectedUnitKey, ExpectedPatch).returning(
          Future.successful(PatchApplied)
        )
        val action = controller.patchVatUnitLinks(VatRef, Period.asString(SamplePeriod))
        val request = FakeRequest().withHeaders(CONTENT_TYPE -> JsonPatchMediaType).withBody(PatchBody)

        val response = call(action, request)

        status(response) shouldBe NO_CONTENT
      }

      "returns CONFLICT when the change conflicts with that of another user" in new EditFixture {
        (patchService.applyPatchTo _).expects(ExpectedUnitKey, ExpectedPatch).returning(
          Future.successful(PatchConflicted)
        )
        val action = controller.patchVatUnitLinks(VatRef, Period.asString(SamplePeriod))
        val request = FakeRequest().withHeaders(CONTENT_TYPE -> JsonPatchMediaType).withBody(PatchBody)

        val response = call(action, request)

        status(response) shouldBe CONFLICT
      }

      "returns UNSUPPORTED MEDIA TYPE when the patch does not have the Json Patch Content-Type" in new EditFixture {
        val action = controller.patchVatUnitLinks(VatRef, Period.asString(SamplePeriod))
        val request = FakeRequest().withHeaders(CONTENT_TYPE -> JSON).withBody(PatchBody)

        val response = call(action, request)

        status(response) shouldBe UNSUPPORTED_MEDIA_TYPE
      }

      "returns BAD REQUEST when the patch is not a valid Json document" in new EditFixture {
        val action = controller.patchVatUnitLinks(VatRef, Period.asString(SamplePeriod))
        // semicolons instead of commas ...
        val invalidJson = s"""[{"op": "test"; "path": "/parents/LEU"; "value": "${IncorrectLegalUnitId.value}"}]"""
        val request = FakeRequest().withHeaders(CONTENT_TYPE -> JsonPatchMediaType).withBody(invalidJson)

        val response = call(action, request)

        status(response) shouldBe BAD_REQUEST
      }

      "returns BAD REQUEST when the patch does not comply with the Json Patch specification (RFC6902)" in new EditFixture {
        val action = controller.patchVatUnitLinks(VatRef, Period.asString(SamplePeriod))
        val invalidPatch = s"""[{"op": "test", "value": "${IncorrectLegalUnitId.value}"}]""" // missing path
        val request = FakeRequest().withHeaders(CONTENT_TYPE -> JsonPatchMediaType).withBody(invalidPatch)

        val response = call(action, request)

        status(response) shouldBe BAD_REQUEST
      }

      "returns UNPROCESSABLE ENTITY when a patch complies with the Json Patch specification but is rejected" in new EditFixture {
        (patchService.applyPatchTo _).expects(ExpectedUnitKey, *).returning(
          Future.successful(PatchRejected)
        )
        val action = controller.patchVatUnitLinks(VatRef, Period.asString(SamplePeriod))
        val request = FakeRequest().withHeaders(CONTENT_TYPE -> JsonPatchMediaType).withBody(PatchBody)

        val response = call(action, request)

        status(response) shouldBe UNPROCESSABLE_ENTITY
      }

      "returns NOT FOUND when a patch targets an entity that does not exist" in new EditFixture {
        (patchService.applyPatchTo _).expects(ExpectedUnitKey, *).returning(
          Future.successful(PatchTargetNotFound)
        )
        val action = controller.patchVatUnitLinks(VatRef, Period.asString(SamplePeriod))
        val request = FakeRequest().withHeaders(CONTENT_TYPE -> JsonPatchMediaType).withBody(PatchBody)

        val response = call(action, request)

        status(response) shouldBe NOT_FOUND
      }

      "returns INTERNAL SERVER ERROR when an attempt to apply a valid patch fails" in new EditFixture {
        (patchService.applyPatchTo _).expects(ExpectedUnitKey, *).returning(
          Future.successful(PatchFailed)
        )
        val action = controller.patchVatUnitLinks(VatRef, Period.asString(SamplePeriod))
        val request = FakeRequest().withHeaders(CONTENT_TYPE -> JsonPatchMediaType).withBody(PatchBody)

        val response = call(action, request)

        status(response) shouldBe INTERNAL_SERVER_ERROR
      }
    }
  }
}
