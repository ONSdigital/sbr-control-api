package uk.gov.ons.sbr.models.units

import io.swagger.annotations.ApiModelProperty

import scala.collection.JavaConversions._

import play.api.libs.json.{ JsValue, Json, OFormat }

import uk.gov.ons.sbr.data.domain.Enterprise

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
)

object EnterpriseUnit {

  implicit val unitFormat: OFormat[EnterpriseUnit] = Json.format[EnterpriseUnit]

  def apply(o: Enterprise): EnterpriseUnit = {
    val childJson = o.getChildren.map {
      a => Json.parse(a.toUnitHierarchyAsJson)
    }.toList
    EnterpriseUnit(o.getKey.toLong, o.getReferencePeriod.toString, o.getVariables.toMap, o.getType.toString, childJson)
  }

}
