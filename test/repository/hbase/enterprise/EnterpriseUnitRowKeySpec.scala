package repository.hbase.enterprise

import org.scalatest.{ FreeSpec, Matchers }
import support.sample.SampleEnterpriseUnit
import uk.gov.ons.sbr.models.enterprise.Ern

class EnterpriseUnitRowKeySpec extends FreeSpec with Matchers with SampleEnterpriseUnit {

  "A Enterprise Unit row key" - {
    "is defined by an Ern (which is reversed to avoid hot-spotting)" in {
      val ern = Ern("823849300")
      EnterpriseUnitRowKey(ern) shouldBe "003948328"
    }
  }
}
