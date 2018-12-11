package controllers.v1

import controllers.AbstractSbrController
import controllers.v1.ControllerResultProcessor._
import controllers.v1.api.ReportingUnitApi
import io.swagger.annotations.Api
import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json._
import play.api.mvc._
import repository.ReportingUnitRepository
import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.reportingunit.{ReportingUnit, Rurn}

@Api("Search")
@Singleton
class ReportingUnitController @Inject() (controllerComponents: ControllerComponents,
                                         repository: ReportingUnitRepository) extends AbstractSbrController(controllerComponents) with ReportingUnitApi {
  override def retrieveReportingUnit(ernStr: String, periodStr: String, rurnStr: String): Action[AnyContent] = Action.async {
    repository.retrieveReportingUnit(Ern(ernStr), Period.fromString(periodStr), Rurn(rurnStr)).map { errorOrReportingUnit =>
      errorOrReportingUnit.fold(resultOnFailure, resultOnSuccessWithAtMostOneUnit[ReportingUnit])
    }
  }

  override def retrieveAllReportingUnitsForEnterprise(ernStr: String, periodStr: String): Action[AnyContent] = Action.async {
    repository.findReportingUnitsForEnterprise(Ern(ernStr), Period.fromString(periodStr)).map { errorOrReportingUnits =>
      errorOrReportingUnits.fold(resultOnFailure, resultOnSuccessWithMaybeManyUnits)
    }
  }

  private def resultOnSuccessWithMaybeManyUnits(reportingUnits: Seq[ReportingUnit]): Result =
    if (reportingUnits.isEmpty) NotFound
    else Ok(toJson(reportingUnits))
}
