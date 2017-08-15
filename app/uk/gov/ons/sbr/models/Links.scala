package uk.gov.ons.sbr.models

import io.swagger.annotations.ApiModelProperty
import play.api.libs.json.{ JsValue, Json, OFormat }
import uk.gov.ons.sbr.data.domain.StatisticalUnit
import uk.gov.ons.sbr.data.domain.UnitType

import scala.collection.JavaConversions._

/**
 * Created by haqa on 08/08/2017.
 */

case class Links(
  @ApiModelProperty(value = "Unit identifier", example = "", required = true, hidden = false) id: String,
  @ApiModelProperty(value = "A map of parents of returned id [Type, Value]", example = "",
    dataType = "Map[String,String]") parents: Map[String, String],
  @ApiModelProperty(value = "A string of all related children", example = "") children: String,
  @ApiModelProperty(value = "Type of Unit returned", example = "") unitType: String
)

object Links {

  implicit val unitFormat: OFormat[Links] = Json.format[Links]

  // rep play write
  def toStringMap(x: Map[UnitType, String]): Map[String, String] = { x map { case (k, v) => k.toString -> v } }

  def toCC(s: List[StatisticalUnit]): List[Links] = s map (u => Links(
    u.getKey, toStringMap(u.getLinks.getParents.toMap), u.getLinks.getChildJsonString, u.getType.toString
  ))

  def toJson(u: List[StatisticalUnit]): JsValue = Json.toJson(toCC(u))

}

