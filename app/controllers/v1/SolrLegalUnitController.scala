package controllers.v1

import javax.inject.Inject
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.api.mvc.{ Action, AnyContent, Controller, Result }
import repository.solr.SolrLegalUnitRepository
import uk.gov.ons.sbr.models.legalunit.Ubrn

class SolrLegalUnitController @Inject() (repository: SolrLegalUnitRepository) extends Controller {
  def solrLegalUnit(ubrnStr: String): Action[AnyContent] = Action.async {
    repository.find(Ubrn(ubrnStr)).map {
      _.fold(
        err => InternalServerError(err),
        _.fold[Result](NotFound)(leu => Ok(Json.toJson(leu)))
      )
    }
  }
}
