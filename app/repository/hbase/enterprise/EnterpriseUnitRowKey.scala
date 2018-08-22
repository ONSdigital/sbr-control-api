package repository.hbase.enterprise

import uk.gov.ons.sbr.models.enterprise.Ern

object EnterpriseUnitRowKey {
  def apply(ern: Ern): String =
    Ern.reverse(ern)
}
