package uk.gov.ons.sbr.models.unitlinks

import scala.util.Try

import play.api.libs.json._

sealed trait UnitType

object UnitType {
  case object CompaniesHouse extends UnitType
  case object ValueAddedTax extends UnitType
  case object PayAsYourEarnTax extends UnitType

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

  def fromString(unitTypeStr: String): Try[UnitType] =
    Try(fromAcronym(unitTypeStr))

  def toAcronym(unitType: UnitType): String =
    unitType match {
      case CompaniesHouse => Acronym.CompaniesHouse
      case ValueAddedTax => Acronym.ValueAddedTax
      case PayAsYourEarnTax => Acronym.PayeAsYourEarnTax

      case Enterprise => Acronym.Enterprise
      case LegalUnit => Acronym.LegalUnit
      case LocalUnit => Acronym.LocalUnit
      case ReportingUnit => Acronym.ReportingUnit
    }

  def fromAcronym(acronym: String): UnitType =
    acronym match {
      case Acronym.CompaniesHouse => CompaniesHouse
      case Acronym.ValueAddedTax => ValueAddedTax
      case Acronym.PayeAsYourEarnTax => PayAsYourEarnTax

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