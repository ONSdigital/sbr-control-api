package uk.gov.ons.sbr.models.unitlinks

import play.api.libs.json.{ Json, OWrites }
import io.swagger.annotations.ApiModelProperty

import uk.gov.ons.sbr.models.Period

//@ TODO - ADD examples for parent and children
case class UnitLinks(
  @ApiModelProperty(value = "Unit Link identifier", example = "1000000090782312~LEU~201802", dataType = "String", required = true) id: String,
  @ApiModelProperty(value = "Period (unit load date) - in YYYYMM format", example = "201802", dataType = "uk.gov.ons.sbr.models.Period", required = true) period: Period,
  @ApiModelProperty(value = "A map of parents of returned id [{Type, Value}]", example = """{"ENT": "1000000033"}""", dataType = "Map[String,String]", required = false) parents: Option[Map[String, String]],
  @ApiModelProperty(value = "A map listing of children units [{Id, Type}]", example = """{"401347263289": "VAT", "01752564": "CH"}""", dataType = "Map[String,String]", required = false) children: Option[Map[String, String]],
  @ApiModelProperty(value = "The Statistical unit type", dataType = "uk.gov.ons.sbr.models.unitlinks.UnitType", example = "ENT", required = true) unitType: UnitType
)

object UnitLinks {
  implicit val unitFormat: OWrites[UnitLinks] = Json.writes[UnitLinks]
}
