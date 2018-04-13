package uk.gov.ons.sbr.models

import java.time.{ Month, YearMonth }
import java.time.format.DateTimeFormatter

import scala.util.Try

case class Period(value: YearMonth)

object Period {
  private val FormatPattern = "uuuuMM"

  private def formatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern(FormatPattern)

  def parseString(str: String): Try[Period] =
    Try(Period(YearMonth.parse(str, formatter)))

  /**
   * @throws java.time.format.DateTimeParseException when str cannot be parsed successfully
   */
  def fromString(str: String): Period =
    parseString(str).get

  def fromYearMonth(year: Int, month: Month): Period =
    Period(YearMonth.of(year, month))

  def asString(period: Period): String =
    period.value.format(formatter)
}