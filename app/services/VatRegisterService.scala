package services
import javax.inject.Inject
import play.api.http.Status.{ NOT_FOUND, OK }
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.ws.{ WSClient, WSResponse }
import play.mvc.Http.HeaderNames.ACCEPT
import play.mvc.Http.MimeTypes.JSON
import uk.gov.ons.sbr.models.unitlinks.UnitType.ValueAddedTax
import uk.gov.ons.sbr.models.{ Period, UnitKey }
import utils.BaseUrl

import scala.concurrent.Future

/*
 * We cannot rely on Unit Links to determine whether a VAT reference is valid, because we will only pull a VAT
 * into the register if it already has a relationship to a Legal Unit.  We must therefore contact the admin data
 * service.  Note that we use a HEAD request as we are only interested in the status code, and not the body.
 */
class VatRegisterService @Inject() (baseUrl: BaseUrl, wsClient: WSClient) extends UnitRegisterService {

  override def isRegisteredUnit(unitKey: UnitKey): Future[UnitRegisterResult] = {
    require(unitKey.unitType == ValueAddedTax)
    wsClient.url(urlFor(unitKey)).withHeaders(ACCEPT -> JSON).head().map {
      fromResponseToUnitRegisterResult
    }.recover {
      case cause: Throwable => UnitRegisterFailure(cause.getMessage)
    }
  }

  private def urlFor(unitKey: UnitKey): String =
    BaseUrl.asUrlString(baseUrl) + s"/v1/records/${unitKey.unitId.value}/periods/${Period.asString(unitKey.period)}"

  private def fromResponseToUnitRegisterResult(response: WSResponse): UnitRegisterResult =
    response.status match {
      case OK => UnitFound
      case NOT_FOUND => UnitNotFound
      case _ => UnitRegisterFailure(describeStatus(response))
    }

  private def describeStatus(response: WSResponse): String =
    s"${response.statusText} (${response.status})"
}
