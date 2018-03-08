package uk.gov.ons.sbr.models

/**
 * Created by coolit on 08/03/2018.
 */
sealed trait DbResponse
sealed trait DbErrorMsg extends DbResponse {
  val msg: String
}

//case class DbSuccessEnterprise(result: EnterpriseUnit) extends DbSuccess[EnterpriseUnit]
//case class DbSuccessUnitLinks(result: UnitLinks) extends DbSuccess[UnitLinks]
//case class DbSuccessUnitLinksList(result: List[UnitLinks]) extends DbSuccess[List[UnitLinks]]

case class DbResult[T](result: T) extends DbResponse
case class DbNotFound(msg: String = "Not Found") extends DbErrorMsg
case class DbTimeout(msg: String) extends DbErrorMsg
case class DbServerError(msg: String) extends DbErrorMsg
