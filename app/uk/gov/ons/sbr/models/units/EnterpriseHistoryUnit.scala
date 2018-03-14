package uk.gov.ons.sbr.models.units

import io.swagger.annotations.ApiModelProperty
import play.api.libs.json.{ Json, OFormat }
import uk.gov.ons.sbr.models.DataUnit

/**
 * Created by urquha 13/03/2018 .
 */

case class EnterpriseHistoryUnit(
  @ApiModelProperty(example = "", dataType = "java.lang.Long") id: String,
  period: Int,
  @ApiModelProperty(value = "A key value pair of all variables associated", example = "",
    dataType = "Map[String,String]") vars: Map[String, String],
  unitType: String,
  childrenJson: List[LEU]
) extends DataUnit[String]

object EnterpriseHistoryUnitUnit {

  implicit val unitFormat: OFormat[EnterpriseHistoryUnit] = Json.format[EnterpriseHistoryUnit]
}
