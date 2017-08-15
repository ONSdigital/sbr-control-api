package uk.gov.ons.sbr.models.units

import io.swagger.annotations.ApiModelProperty
import play.api.libs.json.{ JsValue, Json, OFormat }
import uk.gov.ons.sbr.data.domain.Enterprise

import scala.collection.JavaConversions._
/**
 * Created by haqa on 08/08/2017.
 */

case class EnterpriseKey(
  @ApiModelProperty(value = "Unit identifier", example = "", required = true, hidden = false) id: Long,
  period: String,
  @ApiModelProperty(value = "A key value pair of all variables associated", example = "",
    dataType = "Map[String,String]") values: Map[String, String],
  unitType: String
)

object EnterpriseKey {

  implicit val unitFormat: OFormat[EnterpriseKey] = Json.format[EnterpriseKey]

  def toCC(o: Enterprise): EnterpriseKey = {
    EnterpriseKey(o.getKey.toLong, o.getReferencePeriod.toString, o.getVariables.toMap, o.getType.toString)
  }

  def toJson(o: Enterprise): JsValue = Json.toJson(toCC(o))

}
