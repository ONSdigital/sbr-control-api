package uk.gov.ons.sbr.models.unitlinks

import scala.util.Try

import play.api.libs.json._

sealed trait UnitType

object UnitType {
  case object CompaniesHouse extends UnitType
  case object ValueAddedTax extends UnitType
  case object PayAsYouEarn extends UnitType

  case object Enterprise extends UnitType
  case object LegalUnit extends UnitType
  case object LocalUnit extends UnitType
  case object ReportingUnit extends UnitType

  private object Acronym {
    val CompaniesHouse = "CH"
    val ValueAddedTax = "VAT"
    val PayeAsYourEarnTax = "PAYE"

    val Enterprise = "ENT"
    val LegalUnit = "LEU"
    val LocalUnit = "LOU"
    val ReportingUnit = "REU"
  }

  /*
   * If a Try is not what you want, fromAcronym is now a PartialFunction - so you can lift this to get an Option[UnitType].
   */
  def fromString(unitTypeStr: String): Try[UnitType] =
    Try(fromAcronym(unitTypeStr))

  def toAcronym(unitType: UnitType): String =
    unitType match {
      case CompaniesHouse => Acronym.CompaniesHouse
      case ValueAddedTax => Acronym.ValueAddedTax
      case PayAsYouEarn => Acronym.PayeAsYourEarnTax

      case Enterprise => Acronym.Enterprise
      case LegalUnit => Acronym.LegalUnit
      case LocalUnit => Acronym.LocalUnit
      case ReportingUnit => Acronym.ReportingUnit
    }

  def fromAcronym: PartialFunction[String, UnitType] = {
    case Acronym.CompaniesHouse => CompaniesHouse
    case Acronym.ValueAddedTax => ValueAddedTax
    case Acronym.PayeAsYourEarnTax => PayAsYouEarn

    case Acronym.Enterprise => Enterprise
    case Acronym.LegalUnit => LegalUnit
    case Acronym.LocalUnit => LocalUnit
    case Acronym.ReportingUnit => ReportingUnit
  }

  implicit val writes: Writes[UnitType] = new Writes[UnitType] {
    override def writes(unitType: UnitType): JsValue =
      JsString(toAcronym(unitType))
  }
}