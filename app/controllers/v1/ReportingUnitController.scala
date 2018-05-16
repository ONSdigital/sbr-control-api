package controllers.v1

import javax.inject.Inject

import controllers.v1.api.ReportingUnitApi
import play.api.mvc.{ Action, AnyContent, Controller, Result }
import repository.ReportingUnitRepository
import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.reportingunit.{ ReportingUnit, Rurn }
import controllers.v1.ControllerResultProcessor._
import play.api.libs.json.Json._

import scala.concurrent.ExecutionContext.Implicits.global

class ReportingUnitController @Inject() (repository: ReportingUnitRepository) extends Controller with ReportingUnitApi {
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

  def badRequest(ernStr: String, periodStr: String, rurnStrOpt: Option[String]) = Action {
    BadRequest
  }
}
