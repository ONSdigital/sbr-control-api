package uk.gov.ons.sbr.models.legalunit

import io.swagger.annotations.ApiModelProperty
import play.api.libs.json.{ Json, OWrites }

import uk.gov.ons.sbr.models.enterprise.EnterpriseLink

case class LegalUnit(
  @ApiModelProperty(value = "Legal Unit Reference Number (UBRN)", dataType = "string", example = "1000000012", required = true) uBRN: UBRN,
  @ApiModelProperty(value = "IDBR Local Unit Reference", dataType = "string", example = "9999999999", required = false) UBRNref: Option[String],
  @ApiModelProperty(value = "A container for links to the associated Enterprise", dataType = "uk.gov.ons.sbr.models.enterprise.EnterpriseLink", required = true) enterprise: EnterpriseLink

)

object LegalUnit {
  implicit val writes: OWrites[LegalUnit] = Json.writes[LegalUnit]
}
