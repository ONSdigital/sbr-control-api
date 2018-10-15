package uk.gov.ons.sbr.models.enterprise

import io.swagger.annotations.ApiModelProperty
import play.api.libs.json._
import uk.gov.ons.sbr.models.{ Address, WritesBigDecimal }

case class Enterprise(
  @ApiModelProperty(value = "Enterprise Reference Number (ERN)", dataType = "string", example = "1100000001", required = true) ern: Ern,
  @ApiModelProperty(value = "IDBR Enterprise Reference", dataType = "string", example = "9990009991", required = false) entref: Option[String],
  @ApiModelProperty(value = "Name for the enterprise", dataType = "string", example = "Big Box Cereal", required = true) name: String,
  @ApiModelProperty(value = "Trading as name / alternative name", dataType = "string", example = "Big Box Cereal Ltd", required = false) tradingStyle: Option[String],
  @ApiModelProperty(value = "A container for address details", dataType = "uk.gov.ons.sbr.models.Address", required = true) address: Address,
  @ApiModelProperty(value = "Primary Standard  Industrial Classification (SIC)", dataType = "string", example = "10612", required = true) sic07: String,
  @ApiModelProperty(value = "Legal status", dataType = "string", example = "1", required = true) legalStatus: String,
  @ApiModelProperty(value = "Average of PAYE jobs across year", dataType = "int", example = "100", required = false) employees: Option[Int],
  @ApiModelProperty(value = "Sum of PAYE jobs for latest period", dataType = "int", example = "100", required = false) jobs: Option[Int],
  @ApiModelProperty(value = "A container for various turnover calculations", dataType = "uk.gov.ons.sbr.models.enterprise.Turnover", required = false) turnover: Option[Turnover],
  @ApiModelProperty(value = "Permanent Random Number (PRN)", dataType = "string", example = "0.016587362", required = true) prn: BigDecimal,
  @ApiModelProperty(value = "The number of working proprietors", dataType = "int", example = "1", required = true) workingProprietors: Int,
  @ApiModelProperty(value = "The sum of employees and working proprietors", dataType = "int", example = "101", required = true) employment: Int,
  @ApiModelProperty(value = "Geographic region", dataType = "string", example = "E12000001", required = true) region: String,
  @ApiModelProperty(value = "Imputed Variables", dataType = "uk.gov.ons.sbr.models.enterprise.Imputed", required = false) imputed: Option[Imputed]
)

object Enterprise {
  private implicit val writesBigDecimal: Writes[BigDecimal] = WritesBigDecimal
  implicit val writes: OWrites[Enterprise] = Json.writes[Enterprise]
}
