package services

import javax.inject.Inject
import play.api.http.Status.{NOT_FOUND, OK}
import play.api.libs.ws.{WSClient, WSResponse}
import play.mvc.Http.HeaderNames.ACCEPT
import play.mvc.Http.MimeTypes.JSON
import uk.gov.ons.sbr.models.{Period, UnitKey}
import utils.BaseUrl

import scala.concurrent.{ExecutionContext, Future}

/*
 * We cannot rely on Unit Links to determine whether an Admin Data reference is valid, because we will only pull an
 * admin unit into the register if it already has a relationship to a Legal Unit.  We must therefore contact an admin
 * data service.  Note that we use a HEAD request as we are only interested in the status code, and not the body.
 */
class AdminUnitRegisterService @Inject() (baseUrl: BaseUrl,
                                          wsClient: WSClient)
                                         (implicit ec: ExecutionContext) extends UnitRegisterService {

  override def isRegisteredUnit(unitKey: UnitKey): Future[UnitRegisterResult] =
    wsClient.url(urlFor(unitKey)).withHttpHeaders(ACCEPT -> JSON).head().map {
      fromResponseToUnitRegisterResult
    }.recover {
      case cause: Throwable => UnitRegisterFailure(cause.getMessage)
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
