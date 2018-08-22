package uk.gov.ons.sbr.models.legalunit

import io.swagger.annotations.ApiModelProperty
import play.api.libs.json.{ Json, OWrites }
import uk.gov.ons.sbr.models.Address

case class LegalUnit(
  @ApiModelProperty(value = "Legal Unit Reference Number (UBRN)", dataType = "string", example = "1000000012000000", required = true) ubrn: Ubrn,
  @ApiModelProperty(value = "name", dataType = "string", example = "Big Box Cereal Ltd", required = true) name: String,
  @ApiModelProperty(value = "legalStatus", dataType = "string", example = "1", required = true) legalStatus: String,
  @ApiModelProperty(value = "tradingStatus", dataType = "string", example = "A", required = false) tradingStatus: Option[String],
  @ApiModelProperty(value = "tradingStyle", dataType = "string", example = "Tasty Cereal Co", required = false) tradingStyle: Option[String],
  @ApiModelProperty(value = "UK Standard Industrial Classification 2007", dataType = "string", example = "10612", required = true) sic07: String,
  @ApiModelProperty(value = "turnover", dataType = "int", example = "10", required = false) turnover: Option[Int],
  @ApiModelProperty(value = "Number of Jobs", dataType = "int", example = "100", required = false) payeJobs: Option[Int],
  @ApiModelProperty(value = "A container for address details", dataType = "uk.gov.ons.sbr.models.Address", required = true) address: Address,
  @ApiModelProperty(value = "Birth Date", dataType = "string", example = "01/01/2017", required = true) birthDate: String,
  @ApiModelProperty(value = "Death Date", dataType = "string", example = "06/06/2018", required = false) deathDate: Option[String],
  @ApiModelProperty(value = "Death Code", dataType = "string", example = "1", required = false) deathCode: Option[String],
  @ApiModelProperty(value = "Company Reference Number (CRN)", dataType = "string", example = "01245960", required = false) crn: Option[Crn],
  @ApiModelProperty(value = "Unique Property Reference Number (UPRN)", dataType = "string", example = "10023450178", required = false) uprn: Option[Uprn]
)

object LegalUnit {
  implicit val writes: OWrites[LegalUnit] = Json.writes[LegalUnit]
}
