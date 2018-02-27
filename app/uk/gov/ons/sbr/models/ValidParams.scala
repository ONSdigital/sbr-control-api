package uk.gov.ons.sbr.models

import org.joda.time.YearMonth
import org.joda.time.format.DateTimeFormat

import scala.util.{ Failure, Success, Try }

/**
 * Created by coolit on 19/02/2018.
 *
 * I have to call the companion apply methods applyA not apply because they take the same
 * parameters as the normal case class apply method
 * http://www.scala-lang.org/old/node/2211
 *
 */

// @todo - Use dependancy injected config for min/max key length, period format, id regex, valid categories
// @todo - Make ValidParams trait more dynamic, e.g. common apply method for Ent/UnitLinks

sealed trait ValidParams {

  private val minKeyLength: Int = 4
  private val maxKeyLength: Int = 13
  private val periodFormat: String = "yyyyMM"

  def validId(id: String): Boolean = id.length < maxKeyLength && id.length > minKeyLength

  def validPeriod(period: String): Boolean = Try(YearMonth.parse(period), DateTimeFormat.forPattern(periodFormat)) match {
    case Success(_) => true
    case Failure(_) => false
  }
}

case class UnitLinksParams(id: String, period: String) extends ValidParams
object UnitLinksParams extends ValidParams {
  def applyA(id: String, period: String): Either[UnitLinksParams, InvalidParams] = (id, period) match {
    case (id, _) if (!validId(id)) => Right(InvalidId())
    case (_, period) if (!validPeriod(period)) => Right(InvalidPeriod())
    case (id, period) => Left(UnitLinksParams(id, period))
  }
}

case class EnterpriseParams(id: String, period: String) extends ValidParams
object EnterpriseParams extends ValidParams {
  def applyA(id: String, period: String): Either[EnterpriseParams, InvalidParams] = (id, period) match {
    case (id, _) if (!validId(id)) => Right(InvalidId())
    case (_, period) if (!validPeriod(period)) => Right(InvalidPeriod())
    case (id, period) => Left(EnterpriseParams(id, period))
  }
}

case class StatUnitLinksParams(id: String, category: String, period: String) extends ValidParams
object StatUnitLinksParams extends ValidParams {
  private val validCategories: List[String] = List("ENT", "LEU", "VAT", "PAYE", "CH")

  def applyA(id: String, category: String, period: String): Either[StatUnitLinksParams, InvalidParams] = (id, period, category) match {
    case (id, _, _) if (!validId(id)) => Right(InvalidId())
    case (_, period, _) if (!validPeriod(period)) => Right(InvalidPeriod())
    case (_, _, category) if (!validCategory(category)) => Right(InvalidCategory())
    case (id, period, category) => Left(StatUnitLinksParams(id, category, period))
  }

  def validCategory(category: String): Boolean = validCategories.contains(category)
}
