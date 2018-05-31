package uk.gov.ons.sbr.models.legalunit

import io.swagger.annotations.ApiModelProperty
import play.api.libs.json.{ Json, OWrites }
import uk.gov.ons.sbr.models.enterprise.EnterpriseLink
import uk.gov.ons.sbr.models.Address

case class LegalUnit(
  @ApiModelProperty(value = "Legal Unit Reference Number (UBRN)", dataType = "string", example = "1000000012", required = true) ubrn: UBRN,
  @ApiModelProperty(value = "Company Reference Number (crn)", dataType = "String", required = false) crn: Option[String],
  @ApiModelProperty(value = "name", dataType = "string", example = "Company X", required = true) name: String,
  @ApiModelProperty(value = "legalStatus", dataType = "string", example = "", required = true) legalStatus: String,
  @ApiModelProperty(value = "tradingStatus", dataType = "string", example = "", required = true) tradingStatus: String,
  @ApiModelProperty(value = "tradingstyle", dataType = "string", example = "A", required = false) tradingstyle: Option[String],
  @ApiModelProperty(value = "UK Standard Industrial Classification 2007", dataType = "string", example = "21342", required = true) sic07: String,
  @ApiModelProperty(value = "turnover", dataType = "int", example = "A", required = false) turnover: Option[Int],
  @ApiModelProperty(value = "Number of Jobs", dataType = "int", example = "100", required = false) jobs: Option[Int],
  @ApiModelProperty(value = "A container for links to the associated Enterprise", dataType = "uk.gov.ons.sbr.models.enterprise.EnterpriseLink", required = true) enterprise: EnterpriseLink,
  @ApiModelProperty(value = "A container for address details", dataType = "uk.gov.ons.sbr.models.Address", required = true) address: Address
)

object LegalUnit {
  implicit val writes: OWrites[LegalUnit] = Json.writes[LegalUnit]
}
