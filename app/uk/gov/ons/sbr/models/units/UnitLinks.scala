package uk.gov.ons.sbr.models.units

import scala.collection.JavaConversions._

import io.swagger.annotations.ApiModelProperty
import play.api.libs.json.{ Json, OFormat }

import uk.gov.ons.sbr.data.domain.StatisticalUnit
import uk.gov.ons.sbr.models.DataUnit

/**
 * Created by haqa on 08/08/2017.
 */

case class UnitLinks(
  @ApiModelProperty(example = "", dataType = "String") id: String,
  @ApiModelProperty(value = "A map of parents of returned id [Type, Value]", example = "",
    dataType = "Map[String,String]") parents: Option[Map[String, String]],
  @ApiModelProperty(value = "A string of all related children", example = "") children: Option[Map[String, String]],
  @ApiModelProperty(value = "Type of Unit returned", example = "") unitType: String
) extends DataUnit[String]

object UnitLinks {

  implicit val unitFormat: OFormat[UnitLinks] = Json.format[UnitLinks]

  def apply(u: StatisticalUnit): UnitLinks = {
    val parentMap = u.getLinks.getParents match {
      case y if !y.isEmpty =>
        Some(y.map { case (group, id) => group.toString -> id }.toMap)
      case _ => None
    }
    val childrenMap = u.getLinks.getChildren match {
      case x if !x.isEmpty =>
        Some(x.map { case (id, group) => id -> group.toString }.toMap)
      case _ => None
    }
    UnitLinks(u.getKey, parentMap, childrenMap, u.getType.toString)
  }
}