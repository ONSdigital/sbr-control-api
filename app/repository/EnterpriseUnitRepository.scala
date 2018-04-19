package repository

import scala.concurrent.Future

import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.{ Enterprise, Ern }

/**
 * EnterpriseUnitRepository
 * ----------------
 * Author: haqa
 * Date: 18 April 2018 - 16:24
 * Copyright (c) 2017  Office for National Statistics
 */
trait EnterpriseUnitRepository {
  def retrieveEnterpriseUnit(ern: Ern, period: Period): Future[Option[Enterprise]]
}
