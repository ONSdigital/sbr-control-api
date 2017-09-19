package uk.gov.ons.sbr.models

import io.swagger.annotations.ApiModelProperty

import scala.collection.JavaConversions._

import play.api.libs.json.{ JsValue, Json, OFormat }

import uk.gov.ons.sbr.data.domain.StatisticalUnit

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

  def apply(u: StatisticalUnit): UnitLinks = {
    val childMap = u.getLinks.getChildren match {
      case x if !x.isEmpty =>
        Some(x.map { case (id, group) => (id, group.toString) }.toMap)
      case _ => None
    }
    val parentMap = u.getLinks.getParents match {
      case y if !y.isEmpty =>
        Some(y.map { case (id, group) => id.toString -> group }.toMap)
      case _ => None
    }
    UnitLinks(u.getKey, parentMap, childMap, u.getType.toString)
  }

  def toJson(u: List[StatisticalUnit]): JsValue = Json.toJson(u.map(UnitLinks(_)))
}

