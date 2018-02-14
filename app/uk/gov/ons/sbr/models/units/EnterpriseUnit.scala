package uk.gov.ons.sbr.models.units

import scala.collection.JavaConverters._

import io.swagger.annotations.ApiModelProperty
import play.api.libs.json.{ JsValue, Json, OFormat }

import uk.gov.ons.sbr.data.domain.Enterprise
import uk.gov.ons.sbr.data.model.StatUnit
import uk.gov.ons.sbr.models.DataUnit

/**
 * Created by haqa on 08/08/2017.
 */

case class EnterpriseUnit(
  @ApiModelProperty(example = "", dataType = "java.lang.Long") id: Long,
  period: String,
  @ApiModelProperty(value = "A key value pair of all variables associated", example = "",
    dataType = "Map[String,String]") vars: Map[String, String],
  unitType: String,
  childrenJson: List[JsValue]
) extends DataUnit[Long]

object EnterpriseUnit {

  implicit val unitFormat: OFormat[EnterpriseUnit] = Json.format[EnterpriseUnit]

  def apply(o: Enterprise): EnterpriseUnit = {
    val childJson = o.getChildren.asScala.map {
      a => Json.parse(a.toUnitHierarchyAsJson)
    }.toList
    // EnterpriseUnit(o.getKey.toLong, o.getReferencePeriod.toString, o.getVariables.toMap, o.getType.toString, childJson)
    EnterpriseUnit(o.getKey.toLong, o.getReferencePeriod.toString, o.getVariables.asScala.toMap, o.getType.toString, childJson)
  }

  def apply(e: StatUnit): EnterpriseUnit = {
    val childJson = e.children.map { x => Json.toJson(ChildUnit(x)) }.toList
    EnterpriseUnit(e.key.toLong, e.refPeriod.toString, e.variables, e.unitType, childJson)
  }

}
