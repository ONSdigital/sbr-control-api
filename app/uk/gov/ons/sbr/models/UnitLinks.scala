package uk.gov.ons.sbr.models

import io.swagger.annotations.ApiModelProperty
import play.api.libs.json.{ JsValue, Json, OFormat }
import uk.gov.ons.sbr.data.domain.StatisticalUnit
import uk.gov.ons.sbr.data.domain.UnitType

import scala.collection.JavaConversions._

/**
 * Created by haqa on 08/08/2017.
 */

case class UnitLinks(
  @ApiModelProperty(value = "Unit identifier", example = "", required = true, hidden = false) id: String,
  @ApiModelProperty(value = "A map of parents of returned id [Type, Value]", example = "",
    dataType = "Map[String,String]") parents: Option[Map[String, String]],
  @ApiModelProperty(value = "A string of all related children", example = "") children: Option[Map[String, String]],
  @ApiModelProperty(value = "Type of Unit returned", example = "") unitType: String
)

object UnitLinks {

  implicit val unitFormat: OFormat[UnitLinks] = Json.format[UnitLinks]

  @deprecated("Migrated inside apply", "fix/replace-data-lib - 5 Sep 2017")
  def toStringMap(x: Map[UnitType, String]): Map[String, String] = { x map { case (k, v) => k.toString -> v } }

  @deprecated("Migrated to apply", "fix/unitLink-apply - 6 Sep 2017 15:20")
  def applyDeprecated(s: List[StatisticalUnit]): List[UnitLinks] = s map (u => {
    val childMap = u.getLinks.getChildren.map(
      z => (z._1, z._2.toString)
    ).toMap
    val parentMap: Map[String, String] = u.getLinks.getParents.map {
      case (k, v) => k.toString -> v
    }.toMap
    UnitLinks(u.getKey, Option(parentMap), Option(childMap), u.getType.toString)
  })

  def apply(u: StatisticalUnit): UnitLinks = {
    val childMap = u.getLinks.getChildren match {
      case x if !x.isEmpty =>
        Some(x.map(z => (z._1, z._2.toString)).toMap)
      case _ => None
    }
    val parentMap = u.getLinks.getParents match {
      case y if !y.isEmpty =>
        Some(y.map { case (k, v) => k.toString -> v }.toMap)
      case _ => None
    }
    UnitLinks(u.getKey, parentMap, childMap, u.getType.toString)
  }

  @deprecated("Migrated to toJson", "fix/unitLink-apply - 6 Sep 2017 15:20")
  def toJsonDeprecated(u: List[StatisticalUnit]): JsValue = Json.toJson(applyDeprecated(u))

  def toJson(u: List[StatisticalUnit]): JsValue = Json.toJson(u.map(UnitLinks(_)))
}

