package repository.hbase.reportingunit

import org.scalatest.{ FreeSpec, Matchers }
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.reportingunit.Rurn

class ReportingUnitQuerySpec extends FreeSpec with Matchers {

  "A Reporting Unit query" - {
    "should contain the exact Reporting Unit reference number (RURN)" - {
      "when requesting the reporting unit with a specific row key" in {
        ReportingUnitQuery.byRowKey(Ern("1000000014"), Rurn("33000000000")) shouldBe "4100000001~33000000000"
      }
    }

    "should contain a wildcard for the Reporting Unit reference number (RURN)" - {
      "when requesting all reporting units for a given enterprise" in {
        ReportingUnitQuery.forAllWith(Ern("1000000014")) shouldBe "4100000001~*"
      }
    }
  }
}
