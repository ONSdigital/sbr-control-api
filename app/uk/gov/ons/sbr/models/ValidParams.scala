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

  private val minKeyLength: Int = 5
  private val maxKeyLength: Int = 20
  private val periodFormat: String = "yyyyMM"

  def validId(id: String): Boolean = id.length < (maxKeyLength + 1) && id.length > (minKeyLength - 1)

  def validPeriod(period: String): Boolean = Try(YearMonth.parse(period, DateTimeFormat.forPattern(periodFormat))).isSuccess
}

case class UnitLinksParams(id: String) extends ValidParams
object UnitLinksParams extends ValidParams {
  def validate(id: String): Either[UnitLinksParams, InvalidParams] = id match {
    case i if (!validId(i)) => Right(InvalidId())
    case i => Left(UnitLinksParams(i))
  }
}

case class EnterpriseParams(id: String, period: Option[String]) extends ValidParams
object EnterpriseParams extends ValidParams {
  def validate(id: String, period: Option[String]): Either[EnterpriseParams, InvalidParams] = (id, period) match {
    case (i, _) if (!validId(i)) => Right(InvalidId())
    case (_, Some(p)) if (!validPeriod(p)) => Right(InvalidPeriod())
    case (i, p) => Left(EnterpriseParams(i, p))
  }
}

case class StatUnitLinksParams(id: String, category: String, period: String) extends ValidParams
object StatUnitLinksParams extends ValidParams {
  private val validCategories: List[String] = List("ENT", "LEU", "VAT", "PAYE", "CH")

  def validate(id: String, period: String, category: String): Either[StatUnitLinksParams, InvalidParams] = (id, period, category) match {
    case (i, _, _) if (!validId(i)) => Right(InvalidId())
    case (_, p, _) if (!validPeriod(p)) => Right(InvalidPeriod())
    case (_, _, c) if (!validCategory(c)) => Right(InvalidCategory())
    case (i, p, c) => Left(StatUnitLinksParams(i, c, p))
  }

  def validCategory(category: String): Boolean = validCategories.contains(category)
}
