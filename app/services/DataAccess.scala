package services

import uk.gov.ons.sbr.models.units.{ EnterpriseUnit, UnitLinks }

/**
 * Created by coolit on 01/02/2018.
 */
trait DataAccess {

  def getUnitLinks(id: String, period: String): Option[List[UnitLinks]]

  def getEnterprise(id: String, period: String): Option[EnterpriseUnit]

  def getStatUnitLinks(id: String, category: String, period: String): Option[UnitLinks]
}
