package uk.gov.ons.sbr.models.units

import scala.collection.JavaConversions._

import io.swagger.annotations.ApiModelProperty
import play.api.libs.json.{ JsValue, Json, OFormat }

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
}
