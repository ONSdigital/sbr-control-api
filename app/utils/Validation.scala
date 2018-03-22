package utils

import org.joda.time.YearMonth
import org.joda.time.format.DateTimeFormat

import scala.util.Try

/**
 * Created by coolit on 16/03/2018.
 */
object Validation {
  private val minKeyLength: Int = 4
  private val periodFormat: String = "yyyyMM"
  private val validCategories: Set[String] = Set("ENT", "LEU", "VAT", "PAYE", "CH")

  def validId(id: String): Boolean = id.length >= minKeyLength

  def validPeriod(period: String): Boolean = Try(YearMonth.parse(period, DateTimeFormat.forPattern(periodFormat))).isSuccess

  def validCategory(category: String): Boolean = validCategories.contains(category)
}
