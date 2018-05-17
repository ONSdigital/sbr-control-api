package repository.hbase.enterprise

import java.time.Month.FEBRUARY

import org.scalatest.{ FreeSpec, Matchers }

import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.Ern

import support.sample.SampleEnterpriseUnit

class EnterpriseUnitRowKeySpec extends FreeSpec with Matchers with SampleEnterpriseUnit {

  "A Enterprise Unit row key" - {
    "is defined by a Ern (Enterprise identifier number) and a period (as yyyyMM)" in {
      val ern = Ern("823849300")
      EnterpriseUnitRowKey(ern, Period.fromYearMonth(2018, FEBRUARY)) shouldBe "003948328~201802"
    }
  }

}
