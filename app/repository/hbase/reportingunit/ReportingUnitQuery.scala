package repository.hbase.reportingunit

import repository.hbase.HBase.{ RowKeyDelimiter, Wildcard }
import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.reportingunit.Rurn

object ReportingUnitQuery {
  def byRowKey(ern: Ern, period: Period, rurn: Rurn): String =
    queryBy(ern, period, rurnSelector = rurn.value)

  def forAllWith(ern: Ern, period: Period): String =
    queryBy(ern, period, rurnSelector = Wildcard)

  private def queryBy(ern: Ern, period: Period, rurnSelector: String): String = {
    val ernStr = Ern.reverse(ern)
    val periodStr = Period.asString(period)
    Seq(ernStr, periodStr, rurnSelector).mkString(RowKeyDelimiter)
  }
}
