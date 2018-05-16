package controllers.v1

import javax.inject.{ Inject, Singleton }

import com.typesafe.scalalogging.LazyLogging

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json.toJson
import play.api.mvc.{ Action, AnyContent, Controller, Result }
import io.swagger.annotations.Api

import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.legalunit.{ LegalUnit, UBRN }

import controllers.v1.ControllerResultProcessor._
import controllers.v1.api.LegalUnitApi
import repository.LegalUnitRepository

/*
 * Note that we are relying on regex patterns in the routes definitions to apply argument validation.
 * Only requests with valid arguments should be routed to the retrieve... actions.
 * All other requests should be routed to the badRequest action.
 */
@Api("Search")
@Singleton
class LegalUnitController @Inject() (repository: LegalUnitRepository) extends Controller with LegalUnitApi with LazyLogging {
  override def retrieveLegalUnit(ernStr: String, periodStr: String, uBRNStr: String): Action[AnyContent] = Action.async {
    repository.retrieveLegalUnit(Ern(ernStr), Period.fromString(periodStr), UBRN(uBRNStr)).map { errorOrLegalUnit =>
      errorOrLegalUnit.fold(resultOnFailure, resultOnSuccessWithAtMostOneUnit[LegalUnit])
    }
  }
  override def retrieveAllLegalUnitsForEnterprise(ernStr: String, periodStr: String): Action[AnyContent] = Action.async {
    repository.findLegalUnitsForEnterprise(Ern(ernStr), Period.fromString(periodStr)).map { errorOrLocalUnits =>
      errorOrLocalUnits.fold(resultOnFailure, resultOnSuccessWithMaybeManyUnits)
    }
  }
  private def resultOnSuccessWithMaybeManyUnits(localUnits: Seq[LegalUnit]): Result =
    if (localUnits.isEmpty) NotFound
    else Ok(toJson(localUnits))

  def badRequest(ernStr: String, periodStr: String, uBRNStrOpt: Option[String]) = Action {
    BadRequest
  }
}

