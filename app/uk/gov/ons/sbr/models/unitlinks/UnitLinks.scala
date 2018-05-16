package uk.gov.ons.sbr.models.unitlinks

import play.api.libs.json._
import io.swagger.annotations.ApiModelProperty

import uk.gov.ons.sbr.models.Period

//@ TODO - ADD examples for parent and children
case class UnitLinks(
  @ApiModelProperty(value = "Unit Link identifier", example = "1000000090782312~LEU~201802", dataType = "uk.gov.ons.sbr.models.unitlinks.UnitId", required = true) id: UnitId,
  @ApiModelProperty(value = "Period (unit load date) - in YYYYMM format", example = "201802", dataType = "uk.gov.ons.sbr.models.Period", required = true) period: Period,
  @ApiModelProperty(value = "A map of parents of returned id [{Type, Value}]", example = """{"ENT": "1000000033"}""", dataType = "Map[String,String]", required = false) parents: Option[Map[UnitType, UnitId]],
  @ApiModelProperty(value = "A map listing of children units [{Id, Type}]", example = """{"401347263289": "VAT", "01752564": "CH"}""", dataType = "Map[String,String]", required = false) children: Option[Map[UnitId, UnitType]],
  @ApiModelProperty(value = "The Statistical unit type", dataType = "uk.gov.ons.sbr.models.unitlinks.UnitType", example = "ENT", required = true) unitType: UnitType
)

object UnitLinks {

  implicit val unitFormat: Writes[UnitLinks] = WritesLinkedUnit

  private object WritesLinkedUnit extends Writes[UnitLinks] {
    private case class ExternalForm(id: UnitId, period: Period, parents: Option[Map[String, UnitId]], children: Option[Map[String, UnitType]], unitType: UnitType)

    private implicit val writesUnitType: Writes[UnitType] = UnitType.writes
    private implicit val writesUnitId: Writes[UnitId] = UnitId.writes
    private implicit val writesExternalForm: Writes[ExternalForm] = Json.writes[ExternalForm]

    override def writes(ul: UnitLinks): JsValue =
      writesExternalForm.writes(toExternalForm(ul))

    private def toExternalForm(ul: UnitLinks): ExternalForm =
      ExternalForm(
        ul.id, ul.period, ul.parents.map(toExtOptParentsMap), ul.children.map(toExtOptChildrenMap),
        ul.unitType
      )

    private def toExtOptParentsMap(optParents: Map[UnitType, UnitId]): Map[String, UnitId] =
      optParents.map {
        case (unitType, id) =>
          UnitType.toAcronym(unitType) -> id
      }

    private def toExtOptChildrenMap(optChildren: Map[UnitId, UnitType]): Map[String, UnitType] =
      optChildren.map {
        case (id, unitType) =>
          id.value -> unitType
      }
  }
}
