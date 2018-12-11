package controllers.v1

import controllers.AbstractSbrController
import controllers.v1.ControllerResultProcessor._
import controllers.v1.api.EnterpriseUnitApi
import io.swagger.annotations.Api
import javax.inject.{Inject, Singleton}
import play.api.mvc._
import repository.EnterpriseUnitRepository
import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.{Enterprise, Ern}

/*
 * Note that we are relying on regex patterns in the routes definitions to apply argument validation.
 * Only requests with valid arguments should be routed to the retrieve... actions.
 */
@Api("Search")
@Singleton
class EnterpriseUnitController @Inject() (controllerComponents: ControllerComponents,
                                          repository: EnterpriseUnitRepository) extends AbstractSbrController(controllerComponents) with EnterpriseUnitApi {
  def retrieveEnterpriseUnit(ernStr: String, periodStr: String): Action[AnyContent] = Action.async {
    repository.retrieveEnterpriseUnit(Ern(ernStr), Period.fromString(periodStr)).map { errorOrOptEnterprise =>
      errorOrOptEnterprise.fold(resultOnFailure, resultOnSuccessWithAtMostOneUnit[Enterprise])
    }
  }
}
