package repository.hbase.localunit

import java.time.Month.MARCH

import org.scalatest.{ FreeSpec, Matchers }
import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.localunit.Lurn

class LocalUnitQuerySpec extends FreeSpec with Matchers {

  "A Local Unit query" - {
    "should contain the exact Local Unit reference (LURN)" - {
      "when requesting the local unit with a specific row key" in {
        LocalUnitQuery.byRowKey(Ern("1000000014"), Period.fromYearMonth(2018, MARCH), Lurn("900000013")) shouldBe "4100000001~201803~900000013"
      }
    }

    "should contain a wildcard for the Local Unit reference (LURN)" - {
      "when requesting all local units for a given enterprise and period" in {
        LocalUnitQuery.forAllWith(ern = Ern("1000000014"), period = Period.fromYearMonth(2018, MARCH)) shouldBe "4100000001~201803~*"
      }
    }
  }
}
