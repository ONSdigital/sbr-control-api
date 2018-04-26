package controllers.v1

import javax.inject.{ Inject, Singleton }

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json.toJson
import play.api.mvc.{ Action, AnyContent, Controller, Result }
import io.swagger.annotations.Api

import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.localunit.{ LocalUnit, Lurn }

import controllers.v1.ControllerResultProcessor._
import controllers.v1.api.LocalUnitApi
import repository.LocalUnitRepository

/*
 * Note that we are relying on regex patterns in the routes definitions to apply argument validation.
 * Only requests with valid arguments should be routed to the retrieve... actions.
 * All other requests should be routed to the badRequest action.
 */
@Api("Search")
@Singleton
class LocalUnitController @Inject() (repository: LocalUnitRepository) extends Controller with LocalUnitApi {
  override def retrieveLocalUnit(ernStr: String, periodStr: String, lurnStr: String): Action[AnyContent] = Action.async {
    repository.retrieveLocalUnit(Ern(ernStr), Period.fromString(periodStr), Lurn(lurnStr)).map { errorOrLocalUnit =>
      errorOrLocalUnit.fold(resultOnFailure, resultOnSuccessWithAtMostOneUnit[LocalUnit])
    }
  }

  override def retrieveAllLocalUnitsForEnterprise(ernStr: String, periodStr: String): Action[AnyContent] = Action.async {
    repository.findLocalUnitsForEnterprise(Ern(ernStr), Period.fromString(periodStr)).map { errorOrLocalUnits =>
      errorOrLocalUnits.fold(resultOnFailure, resultOnSuccessWithMaybeManyUnits)
    }
  }

  private def resultOnSuccessWithMaybeManyUnits(localUnits: Seq[LocalUnit]): Result =
    if (localUnits.isEmpty) NotFound
    else Ok(toJson(localUnits))

  def badRequest(ernStr: String, periodStr: String, lurnStrOpt: Option[String]) = Action {
    BadRequest
  }
}
