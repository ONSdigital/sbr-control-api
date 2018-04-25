package repository.hbase.localunit

import repository.hbase.HBase.{ RowKeyDelimiter, Wildcard }
import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.localunit.Lurn

object LocalUnitQuery {
  def byRowKey(ern: Ern, period: Period, lurn: Lurn): String =
    queryBy(ern, period, lurnSelector = lurn.value)

  def forAllWith(ern: Ern, period: Period): String =
    queryBy(ern, period, lurnSelector = Wildcard)

  private def queryBy(ern: Ern, period: Period, lurnSelector: String): String = {
    val ernStr = Ern.reverse(ern)
    val periodStr = Period.asString(period)
    Seq(ernStr, periodStr, lurnSelector).mkString(RowKeyDelimiter)
  }
}
