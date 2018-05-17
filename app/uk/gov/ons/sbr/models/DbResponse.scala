package uk.gov.ons.sbr.models

import uk.gov.ons.sbr.models.units.{ EnterpriseUnit, UnitLinksUnit }

/**
 * TODO: a generic case class for DbResult[T] could possibly be used
 */
sealed trait DbResponse
sealed trait DbErrorMsg extends DbResponse {
  val msg: String
}

case class DbSuccessEnterprise(result: EnterpriseUnit) extends DbResponse
case class DbSuccessUnitLinks(result: UnitLinksUnit) extends DbResponse
case class DbSuccessUnitLinksList(result: List[UnitLinksUnit]) extends DbResponse
case class DbNotFound(msg: String = "Not Found") extends DbErrorMsg
case class DbTimeout(msg: String = "Timeout") extends DbErrorMsg
case class DbServiceUnavailable(msg: String = "Service Unavailable") extends DbErrorMsg
case class DbServerError(msg: String = "Internal Server Error") extends DbErrorMsg
