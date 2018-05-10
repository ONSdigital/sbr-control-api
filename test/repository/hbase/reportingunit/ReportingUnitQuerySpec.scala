package repository.hbase.reportingunit

import java.time.Month._

import org.scalatest.{ FreeSpec, Matchers }
import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.reportingunit.Rurn

class ReportingUnitQuerySpec extends FreeSpec with Matchers {

  "A Reporting Unit query" - {
    "should contain the exact Reporting Unit reference number (RURN)" - {
      "when requesting the reporting unit with a specific row key" in {
        ReportingUnitQuery.byRowKey(Ern("1000000014"), Period.fromYearMonth(2018, MARCH), Rurn("900000013")) shouldBe "4100000001~201803~900000013"
      }
    }

    "should contain a wildcard for the Reporting Unit reference number (RURN)" - {
      "when requesting all reporting units for a given enterprise and period" in {
        ReportingUnitQuery.forAllWith(ern = Ern("1000000014"), period = Period.fromYearMonth(2018, MARCH)) shouldBe "4100000001~201803~*"
      }
    }
  }

}
