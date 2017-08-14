package uk.gov.ons.sbr.models.units

import io.swagger.annotations.ApiModelProperty
import play.api.libs.json.{ JsValue, Json, OFormat }
import uk.gov.ons.sbr.data.domain.Enterprise

import scala.collection.JavaConversions._
/**
 * Created by haqa on 08/08/2017.
 */

case class EnterpriseKey(
  @ApiModelProperty(value = "Unit identifier", example = "", required = true, hidden = false) id: String,
  period: String,
  unitType: String,
  @ApiModelProperty(value = "A key value pair of all variables associated", example = "",
    dataType = "Map[String,String]") values: Map[String, String]
)

object EnterpriseKey {

  implicit val unitFormat: OFormat[EnterpriseKey] = Json.format[EnterpriseKey]

  @deprecated("Migrated to fromMap", "test/hbaseUtility [Mon 14 Aug 2017 - 13:48]")
  def addKey(o: Enterprise): Map[String, String] = o.getVariables.toMap + ("key" -> o.getKey)

  def toCC(o: Enterprise): EnterpriseKey = {
    EnterpriseKey(o.getKey, o.getReferencePeriod.toString, o.getType.toString, o.getVariables.toMap)
  }

  def toJson(o: Enterprise): JsValue = Json.toJson(toCC(o))

}
