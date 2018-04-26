package repository.hbase

import org.scalatest.{ FreeSpec, Matchers }

class HBaseSpec extends FreeSpec with Matchers {
  "A HBase" - {
    "REST url can be built" in {
      val url = HBase.rowKeyUrl(protocolWithHostname = "http://hostname", port = "1234", "namespace", "table", "rowKey", "columnFamily")

      url shouldBe "http://hostname:1234/namespace:table/rowKey/columnFamily"
    }
  }
}
