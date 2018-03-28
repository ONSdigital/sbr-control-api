package repository.hbase

import java.time.Month.MARCH

import org.scalatest.{ FreeSpec, Matchers }
import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.localunit.Lurn

class LocalUnitRowKeySpec extends FreeSpec with Matchers {

  "A LocalUnit row key" - {
    "is defined by the Enterprise reference (ERN) (which is reversed), along with the period and Local Unit reference (LURN)" in {
      LocalUnitRowKey(Ern("1000000014"), Period.fromYearMonth(2018, MARCH), Lurn("900000013")) shouldBe "4100000001~201803~900000013"
    }
  }
}
