package uk.gov.ons.sbr.models

import uk.gov.ons.sbr.models.units.{ EnterpriseUnit, UnitLinks }

import scala.concurrent.Future

/**
 * Created by coolit on 08/03/2018.
 *
 * Potential change: maybe change NotFound to a Success
 *
 * Or change to:
 * DbResult
 * DbNotFound
 * DbServerError
 *
 * Maybe don't need different Case Classes for different types of success
 */
sealed trait DbResult
sealed trait DbSuccess[T] extends DbResult {
  val result: T
}
sealed trait DbFailure extends DbResult {
  val msg: String
}

case class DbSuccessEnterprise(result: EnterpriseUnit) extends DbSuccess[EnterpriseUnit]
case class DbSuccessUnitLinks(result: UnitLinks) extends DbSuccess[UnitLinks]
case class DbSuccessUnitLinksList(result: List[UnitLinks]) extends DbSuccess[List[UnitLinks]]
case class DbFailureNotFound(msg: String = "Not found") extends DbFailure
case class DbFailureTimeout(msg: String) extends DbFailure
case class DbFailureServerError(msg: String) extends DbFailure

// questions: sealed trait