package controllers.v1

import controllers.v1.ControllerResultProcessor._
import javax.inject.{ Inject, Singleton }
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.{ Action, AnyContent, Controller }
import repository.solr.SolrLegalUnitRepository
import uk.gov.ons.sbr.models.legalunit.{ LegalUnit, Ubrn }

@Singleton
class SolrLegalUnitController @Inject() (solrLegalUnitRepository: SolrLegalUnitRepository) extends Controller {
  def retrieveLegalUnit(ubrnStr: String): Action[AnyContent] = Action.async {
    solrLegalUnitRepository.solrRetrieveLegalUnit(Ubrn(ubrnStr)).map { errorOrLegalUnit =>
      errorOrLegalUnit.fold(resultOnFailure, resultOnSuccessWithAtMostOneUnit[LegalUnit])
    }
  }
}
