package repository.hbase

import org.scalatest.{ FreeSpec, Matchers }
import play.api.libs.json.Json
import repository.RestRepository.Row
import support.HBaseJsonBodyFixture

class HBaseResponseReaderSpec extends FreeSpec with Matchers {

  private trait Fixture extends HBaseJsonBodyFixture {
    val ColumnFamily = "cf"
    val UnusedRowKey = "some-key"

    def parse(hBaseResponseJsonStr: String): Seq[Row] =
      Json.parse(hBaseResponseJsonStr).as[Seq[Row]](HBaseResponseReader.forColumnFamily(ColumnFamily))
  }

  "A HBase REST response reader" - {
    "can successfully parse a valid response containing no rows" in new Fixture {
      parse("""{"Row":[]}""") shouldBe Seq.empty
    }

    "can successfully parse a valid response containing a single row" in new Fixture {
      val responseJsonStr =
        s"""|{"Row":[
            |  {"key":"${hbaseEncode("some-key")}",
            |   "Cell":[
            |     {"column":"${hbaseEncode(s"$ColumnFamily:col1-name")}",
            |      "timestamp":1520333985745,
            |      "$$":"${hbaseEncode("col1-value")}"},
            |     {"column":"${hbaseEncode(s"$ColumnFamily:col2-name")}",
            |      "timestamp":1520333985745,
            |      "$$":"${hbaseEncode("col2-value")}"}
            |   ]}
            |]}""".stripMargin

      parse(responseJsonStr) shouldBe Seq(Row(rowKey = UnusedRowKey, fields = Map(
        "col1-name" -> "col1-value",
        "col2-name" -> "col2-value"
      )))
    }

    "can successfully parse a valid response containing multiple rows" in new Fixture {
      val responseJsonStr =
        s"""|{"Row":[
            |  {"key":"${hbaseEncode("row1-key")}",
            |   "Cell":[
            |     {"column":"${hbaseEncode(s"$ColumnFamily:row1-col1-name")}",
            |      "timestamp":1520333985745,
            |      "$$":"${hbaseEncode("row1-col1-value")}"},
            |     {"column":"${hbaseEncode(s"$ColumnFamily:row1-col2-name")}",
            |      "timestamp":1520333985745,
            |      "$$":"${hbaseEncode("row1-col2-value")}"}
            |   ]},
            |  {"key":"${hbaseEncode("row2-key")}",
            |   "Cell":[
            |     {"column":"${hbaseEncode(s"$ColumnFamily:row2-col1-name")}",
            |      "timestamp":1520333985745,
            |      "$$":"${hbaseEncode("row2-col1-value")}"},
            |     {"column":"${hbaseEncode(s"$ColumnFamily:row2-col2-name")}",
            |      "timestamp":1520333985745,
            |      "$$":"${hbaseEncode("row2-col2-value")}"}
            |   ]}
            |]}""".stripMargin

      parse(responseJsonStr) shouldBe Seq(
        Row(rowKey = "row1-key", fields = Map("row1-col1-name" -> "row1-col1-value", "row1-col2-name" -> "row1-col2-value")),
        Row(rowKey = "row2-key", fields = Map("row2-col1-name" -> "row2-col1-value", "row2-col2-name" -> "row2-col2-value"))
      )
    }
  }
}
