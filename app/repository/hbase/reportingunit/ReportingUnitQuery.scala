package repository.hbase.reportingunit

import repository.hbase.HBase.{ RowKeyDelimiter, Wildcard }
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.reportingunit.Rurn

object ReportingUnitQuery {
  def byRowKey(ern: Ern, rurn: Rurn): String =
    queryBy(ern, rurnSelector = rurn.value)

  def forAllWith(ern: Ern): String =
    queryBy(ern, rurnSelector = Wildcard)

  private def queryBy(ern: Ern, rurnSelector: String): String =
    Seq(Ern.reverse(ern), rurnSelector).mkString(RowKeyDelimiter)
}
