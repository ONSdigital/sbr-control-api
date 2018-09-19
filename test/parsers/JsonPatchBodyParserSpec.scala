package parsers

import akka.stream.scaladsl.Source
import akka.util.ByteString
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ EitherValues, FreeSpec, Matchers }
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import parsers.JsonPatchBodyParser.JsonPatchMediaType
import play.api.http.Status.{ BAD_REQUEST, UNSUPPORTED_MEDIA_TYPE }
import play.api.libs.json.{ JsNumber, JsString }
import play.api.test.FakeRequest
import play.mvc.Http.HeaderNames.CONTENT_TYPE
import play.mvc.Http.MimeTypes.JSON
import uk.gov.ons.sbr.models.patch.{ ReplaceOperation, TestOperation }

/*
 * See https://github.com/playframework/playframework/blob/master/framework/src/play/src/test/scala/play/mvc/RawBodyParserSpec.scala
 * for an example of how a BodyParser can be tested.
 */
class JsonPatchBodyParserSpec extends FreeSpec with Matchers with GuiceOneAppPerSuite with ScalaFutures with EitherValues {

  private trait Fixture {
    implicit val materializer = app.materializer

    val PatchSpec = """|[{"op": "test", "path": "/a/b/c", "value": "foo"},
                       | {"op": "replace", "path": "/a/b/c", "value": 42}]""".stripMargin
  }

  "A body representing a JSON patch specification" - {
    "can be parsed when valid" in new Fixture {
      val request = FakeRequest().withHeaders(CONTENT_TYPE -> JsonPatchMediaType)
      val body = Source.single(ByteString(PatchSpec))

      whenReady(JsonPatchBodyParser(request).run(body)) { result =>
        result.right.value shouldBe Seq(
          TestOperation("/a/b/c", JsString("foo")),
          ReplaceOperation("/a/b/c", JsNumber(42))
        )
      }
    }

    "is rejected" - {
      "when the media type is not that of Json Patch" in new Fixture {
        val request = FakeRequest().withHeaders(CONTENT_TYPE -> JSON)
        val body = Source.single(ByteString(PatchSpec))

        whenReady(JsonPatchBodyParser(request).run(body)) { result =>
          result.left.value.header.status shouldBe UNSUPPORTED_MEDIA_TYPE
        }
      }

      "when the patch document is not valid json" in new Fixture {
        val request = FakeRequest().withHeaders(CONTENT_TYPE -> JsonPatchMediaType)
        val invalidJson = s"""[{"op": "test", "path": "/a/b/c", "value": "foo"]""" // object is not closed correctly
        val body = Source.single(ByteString(invalidJson))

        whenReady(JsonPatchBodyParser(request).run(body)) { result =>
          result.left.value.header.status shouldBe BAD_REQUEST
        }
      }

      "when the patch document does not conform to the Json Patch specification (RFC6902)" in new Fixture {
        val request = FakeRequest().withHeaders(CONTENT_TYPE -> JsonPatchMediaType)
        val invalidPatch = s"""[{"op": "test", "path": "/a/b/c"}]""" // missing 'value' field
        val body = Source.single(ByteString(invalidPatch))

        whenReady(JsonPatchBodyParser(request).run(body)) { result =>
          result.left.value.header.status shouldBe BAD_REQUEST
        }
      }
    }
  }
}
