package uk.gov.ons.sbr.models.units

import io.swagger.annotations.ApiModelProperty
import play.api.libs.json.{ Json, OFormat }
import uk.gov.ons.sbr.models.DataUnit

case class EnterpriseUnit(
  @ApiModelProperty(value = "Unit identifier", required = true, hidden = false, example = "", dataType = "java.lang.Long") id: String,
  period: String,
  @ApiModelProperty(value = "A key value pair of all variables associated", example = "",
    dataType = "Map[String,String]") vars: Map[String, String],
  unitType: String,
  childrenJson: List[LEU]
) extends DataUnit[String]

object EnterpriseUnit {

  implicit val unitFormat: OFormat[EnterpriseUnit] = Json.format[EnterpriseUnit]
}
