package parsers

import akka.util.ByteString
import com.typesafe.scalalogging.LazyLogging
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.JsValue
import play.api.libs.streams.Accumulator
import play.api.mvc.Results.{ BadRequest, UnsupportedMediaType }
import play.api.mvc._
import uk.gov.ons.sbr.models.patch.Patch

object JsonPatchBodyParser extends BodyParser[Patch] with LazyLogging {
  type JsonPatchBodyParserResult = Accumulator[ByteString, Either[Result, Patch]]
  val JsonPatchMediaType = "application/json-patch+json"

  /*
   * We use the underlying 'tolerantJson' parser because it will accept a Content-Type header that is not strictly json
   * (application/json or text/json), whereas the 'json' parser will not.
   */
  override def apply(rh: RequestHeader): JsonPatchBodyParserResult = {
    rh.contentType.filter(_ == JsonPatchMediaType).fold[JsonPatchBodyParserResult](
      Accumulator.done(Left(UnsupportedMediaType))
    ) { _ =>
        BodyParsers.parse.tolerantJson(rh).map { resultOrJsValue =>
          resultOrJsValue.right.flatMap(jsonToBadRequestOrPatch)
        }
      }
  }

  private def jsonToBadRequestOrPatch(jsValue: JsValue): Either[Result, Patch] = {
    val eitherValidationErrorOrPatch = jsValue.validate[Patch].asEither
    eitherValidationErrorOrPatch.left.foreach { errors =>
      logger.error(s"Json document does not conform to Json Patch specification.  Input=[$jsValue], errors=[$errors].")
    }
    eitherValidationErrorOrPatch.left.map(_ => BadRequest)
  }
}
