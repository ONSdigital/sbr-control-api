package uk.gov.ons.sbr.models.units

import io.swagger.annotations.ApiModelProperty
//import play.api.libs.json.{ JsValue, Json, OFormat }
import uk.gov.ons.sbr.data.domain.Enterprise
import play.api.libs.json._

import scala.collection.JavaConversions._
/**
 * Created by haqa on 08/08/2017.
 */

case class EnterpriseUnit(
  @ApiModelProperty(value = "Unit identifier", example = "", required = true, hidden = false) id: Long,
  period: String,
  @ApiModelProperty(value = "A key value pair of all variables associated", example = "",
    dataType = "Map[String,String]") vars: Map[String, String],
  @ApiModelProperty(value = "A map of parents of returned id [Type, Value]", example = "",
    dataType = "Map[String,String]") parents: Map[String, String],
  @ApiModelProperty(value = "A string of all related children", example = "") children: Map[String, String],
  @ApiModelProperty(value = "A string of all related children", example = "") childrenJson: List[JsValue],
  unitType: String
)

object EnterpriseUnit {

  implicit val unitFormat: OFormat[EnterpriseUnit] = Json.format[EnterpriseUnit]

  def apply(o: Enterprise): EnterpriseUnit = {
    val childMap = o.getLinks.getChildren.map(
      z => (z._1, z._2.toString)
    ).toMap
    val childJson = o.getChildren.map {
      a => Json.parse(a.toUnitHierarchyAsJson)
    }.toList
    EnterpriseUnit(o.getKey.toLong, o.getReferencePeriod.toString, o.getVariables.toMap, Map(), childMap, childJson, o.getType.toString)
  }

  def toJson(o: Enterprise): JsValue = Json.toJson(apply(o))

}
