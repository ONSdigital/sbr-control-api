package Services

import javax.inject.Singleton

import uk.gov.ons.sbr.data.DevDummyApp.dbService
import uk.gov.ons.sbr.data.model.StatUnitLinks

/**
 * Created by haqa on 22/09/2017.
 */
@Singleton
class SQLConnect extends DBConnector {

  def getStatUnitLinksByPeriodAndId(id: String) = ???

  def getStatUnitLinksByPeriodAndId (id: String, period: Long): Seq[StatUnitLinks] = {
    dbService.getStatUnitLinks(period, id)
  }

}
