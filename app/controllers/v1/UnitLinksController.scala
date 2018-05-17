package controllers.v1

import javax.inject.Inject

import play.api.mvc.{ Action, AnyContent, Controller }

import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.unitlinks.{ UnitId, UnitLinks, UnitType }

import controllers.v1.ControllerResultProcessor._
import repository.UnitLinksRepository
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import controllers.v1.api.UnitLinksApi

class UnitLinksController @Inject() (repository: UnitLinksRepository) extends Controller with UnitLinksApi {

  def retrieveUnitLinksWithPeriod(unitIdStr: String, periodStr: String, unitTypeStr: String): Action[AnyContent] = Action.async {
    repository.retrieveUnitLinks(UnitId(unitIdStr), UnitType.fromAcronym(unitTypeStr), Period.fromString(periodStr)).map { errorOrOptUnitLinks =>
      errorOrOptUnitLinks.fold(resultOnFailure, resultOnSuccessWithAtMostOneUnit[UnitLinks])
    }
  }

  def badRequest(unitIdStr: String, periodStr: String, unitTypeStr: String) = Action {
    BadRequest
  }

}
