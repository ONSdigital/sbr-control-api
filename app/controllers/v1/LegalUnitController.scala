package controllers.v1

import com.typesafe.scalalogging.LazyLogging
import controllers.v1.ControllerResultProcessor._
import controllers.v1.api.LegalUnitApi
import io.swagger.annotations.Api
import javax.inject.{Inject, Singleton}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json.toJson
import play.api.mvc._
import repository.LegalUnitRepository
import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.legalunit.{LegalUnit, Ubrn}

/*
 * Note that we are relying on regex patterns in the routes definitions to apply argument validation.
 * Only requests with valid arguments should be routed to the retrieve... actions.
 */
@Api("Search")
@Singleton
class LegalUnitController @Inject() (val controllerComponents: ControllerComponents,
                                     repository: LegalUnitRepository) extends BaseController with LegalUnitApi with LazyLogging {
  override def retrieveLegalUnit(ernStr: String, periodStr: String, ubrnStr: String): Action[AnyContent] = Action.async {
    repository.retrieveLegalUnit(Ern(ernStr), Period.fromString(periodStr), Ubrn(ubrnStr)).map { errorOrLegalUnit =>
      errorOrLegalUnit.fold(resultOnFailure, resultOnSuccessWithAtMostOneUnit[LegalUnit])
    }
  }

  override def retrieveAllLegalUnitsForEnterprise(ernStr: String, periodStr: String): Action[AnyContent] = Action.async {
    repository.findLegalUnitsForEnterprise(Ern(ernStr), Period.fromString(periodStr)).map { errorOrLegalUnits =>
      errorOrLegalUnits.fold(resultOnFailure, resultOnSuccessWithMaybeManyUnits)
    }
  }

  private def resultOnSuccessWithMaybeManyUnits(legalUnits: Seq[LegalUnit]): Result =
    if (legalUnits.isEmpty) NotFound
    else Ok(toJson(legalUnits))
}

