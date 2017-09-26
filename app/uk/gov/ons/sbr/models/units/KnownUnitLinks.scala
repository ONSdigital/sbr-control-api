package uk.gov.ons.sbr.models.units

import scala.collection.JavaConversions._

import io.swagger.annotations.ApiModelProperty
import play.api.libs.json.{Json, OFormat}

import uk.gov.ons.sbr.data.domain.StatisticalUnitLinks
import uk.gov.ons.sbr.data.model.StatUnitLinks

/**
 * Created by haqa on 21/09/2017.
 */
case class KnownUnitLinks(
  @ApiModelProperty(example = "", dataType = "String") id: String,
  @ApiModelProperty(value = "A map of parents of returned id [Type, Value]", example = "",
    dataType = "Map[String,String]") parents: Option[Map[String, String]],
  @ApiModelProperty(value = "A string of all related children", example = "") children: Option[Map[String, String]]
)

object KnownUnitLinks {

  implicit val unitFormat: OFormat[KnownUnitLinks] = Json.format[KnownUnitLinks]

  def apply(u: StatisticalUnitLinks): KnownUnitLinks = {
    val parentMap = u.getParents match {
      case y if !y.isEmpty =>
        Some(y.map { case (group, id) => group.toString -> id }.toMap)
      case _ => None
    }
    val childrenMap = u.getChildren match {
      case x if !x.isEmpty =>
        Some(x.map { case (id, group) => id -> group.toString }.toMap)
      case _ => None
    }
    KnownUnitLinks(u.getKey, parentMap, childrenMap)
  }

  def apply(s: StatUnitLinks): KnownUnitLinks = {
    KnownUnitLinks(s.key, Option(s.parents), Option(s.children))
  }

}

