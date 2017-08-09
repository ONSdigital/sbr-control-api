package models

import play.api.libs.json.{ OFormat, Json, JsValue }

import uk.gov.ons.sbr.data.domain.{ StatisticalUnit }
import uk.gov.ons.sbr.data.domain.UnitType

import scala.collection.JavaConversions._
//import scala.collection.immutable.Map

/**
 * Created by haqa on 08/08/2017.
 */

//@ImplementedBy()
case class Links(
  id: String,
  parents: Map[String, String],
  children: String,
  unitType: String
)

object Links {

  implicit val unitFormat: OFormat[Links] = Json.format[Links]

  // rep with play write
  // convert map.UnitType -> map.UnitType.toString
  def toStringMap(m: Map[UnitType, String]): Map[String, String] = { m map { case (k, v) => k.toString -> v } }

  def toCC(x: List[StatisticalUnit]): List[Links] = x.map(u => Links(
    u.getKey,
    toStringMap(u.getLinks.getParents.toMap), u.getLinks.getChildJsonString, u.getType.toString
  ))

  def toJson(u: List[StatisticalUnit]): JsValue = Json.toJson(toCC(u))

}

