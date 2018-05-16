package repository.hbase.legalunit

import repository.hbase.HBase.{ RowKeyDelimiter, Wildcard }
import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.legalunit.UBRN

object LegalUnitQuery {
  def byRowKey(ern: Ern, period: Period, uBRN: UBRN): String =
    queryBy(ern, period, UBRNSelector = uBRN.value)

  def forAllWith(ern: Ern, period: Period): String =
    queryBy(ern, period, UBRNSelector = Wildcard)

  private def queryBy(ern: Ern, period: Period, UBRNSelector: String): String = {
    val ernStr = Ern.reverse(ern)
    val periodStr = Period.asString(period)
    Seq(ernStr, periodStr, UBRNSelector).mkString(RowKeyDelimiter)
  }
}