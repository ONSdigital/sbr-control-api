package uk.gov.ons.sbr.models.enterprise

import play.api.libs.json.{ Json, OWrites }
import io.swagger.annotations.ApiModelProperty

import uk.gov.ons.sbr.models.Address

case class Enterprise(
  @ApiModelProperty(value = "Enterprise Reference Number (ERN)", dataType = "string", example = "1000000012", required = true) ern: Ern,
  @ApiModelProperty(value = "IDBR Enterprise Reference", dataType = "string", example = "9999999999", required = false) entref: Option[String],
  @ApiModelProperty(value = "name", dataType = "string", example = "Company X", required = true) name: String,
  @ApiModelProperty(value = "tradingStyle", dataType = "string", example = "A", required = false) tradingStyle: Option[String],
  @ApiModelProperty(value = "A container for address details", dataType = "uk.gov.ons.sbr.models.Address", required = true) address: Address,
  @ApiModelProperty(value = "UK Standard Industrial Classification 2007", dataType = "string", example = "21342", required = true) sic07: String,
  @ApiModelProperty(value = "Legal status", dataType = "string", example = "NP10 5XJ", required = true) legalStatus: String,
  @ApiModelProperty(value = "Number of employees (source: PAYE)", dataType = "int", example = "100", required = false) employees: Option[Int],
  @ApiModelProperty(value = "Sum of employees for latest period (source: PAYE)", dataType = "int", example = "100", required = false) jobs: Option[Int],
  @ApiModelProperty(value = "A container for various turnover calculations", dataType = "uk.gov.ons.sbr.models.enterprise.Turnover", required = true) turnover: Option[Turnover]
)

object Enterprise {
  implicit val writes: OWrites[Enterprise] = Json.writes[Enterprise]
}
