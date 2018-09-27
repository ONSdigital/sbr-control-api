package repository.hbase

import org.scalatest.{ FreeSpec, Matchers }
import utils.BaseUrl

class HBaseSpec extends FreeSpec with Matchers {

  private trait Fixture {
    val baseUrl = BaseUrl(protocol = "http", host = "hostname", port = 1234, prefix = None)
  }

  "A HBase" - {
    "REST url can be built" - {
      "that specifies a column family (used by GET requests)" in new Fixture {
        val url = HBase.rowKeyColFamilyUrl(baseUrl, "namespace", "table", "rowKey", "columnFamily")

        url shouldBe "http://hostname:1234/namespace:table/rowKey/columnFamily"
      }

      "that specifies a checked put (and does not contain a column family)" in new Fixture {
        val url = HBase.checkedPutUrl(baseUrl, "namespace", "table", "rowKey")

        url shouldBe "http://hostname:1234/namespace:table/rowKey/?check=put"
      }

      "that identifies an entire row (with no column family)" in new Fixture {
        val url = HBase.rowKeyUrl(baseUrl, "namespace", "table", "rowKey")

        url shouldBe "http://hostname:1234/namespace:table/rowKey"
      }
    }
  }
}
