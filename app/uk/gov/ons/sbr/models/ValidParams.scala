package uk.gov.ons.sbr.models

import utils.Validation._

sealed trait ValidParams

case class UnitLinksParams(id: String) extends ValidParams
object UnitLinksParams {
  def validate(id: String): Either[InvalidParams, UnitLinksParams] =
    id match {
      case i if !validId(i) => Left(InvalidId())
      case i => Right(UnitLinksParams(i))
    }
}

case class EnterpriseParams(id: String, period: Option[String]) extends ValidParams
object EnterpriseParams {
  def validate(id: String, period: Option[String]): Either[InvalidParams, EnterpriseParams] =
    (id, period) match {
      case (i, _) if !validId(i) => Left(InvalidId())
      case (_, Some(p)) if !validPeriod(p) => Left(InvalidPeriod())
      case (i, p) => Right(EnterpriseParams(i, p))
    }
}

case class StatUnitLinksParams(id: String, category: String, period: String) extends ValidParams
object StatUnitLinksParams {
  def validate(id: String, category: String, period: String): Either[InvalidParams, StatUnitLinksParams] =
    (id, period, category) match {
      case (i, _, _) if !validId(i) => Left(InvalidId())
      case (_, p, _) if !validPeriod(p) => Left(InvalidPeriod())
      case (_, _, c) if !validCategory(c) => Left(InvalidCategory())
      case (i, p, c) => Right(StatUnitLinksParams(i, c, p))
    }
}
