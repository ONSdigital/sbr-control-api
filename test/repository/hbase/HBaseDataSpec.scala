package repository.hbase

import org.scalatest.{ FreeSpec, Matchers }
import play.api.libs.json.{ Format, JsValue, Json }
import repository.hbase.HBaseData.{ HBaseCell, HBaseRow }
import support.HBaseJsonBodyFixture

class HBaseDataSpec extends FreeSpec with Matchers {

  private trait Fixture extends HBaseJsonBodyFixture {
    val ColumnFamily = "cf"

    private val format: Format[Seq[HBaseRow]] = HBaseData.format

    def read(jsonStr: String): Seq[HBaseRow] =
      Json.parse(jsonStr).as[Seq[HBaseRow]](format)

    def write(data: Seq[HBaseRow]): JsValue =
      Json.toJson(data)(format)
  }

  "HBase data" - {
    "can be successfully read" - {
      "when it represents no rows" in new Fixture {
        read(jsonStr = """{"Row":[]}""") shouldBe Seq.empty
      }

      "when it represents a single row" in new Fixture {
        val jsonStr =
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

        read(jsonStr) shouldBe Seq(
          HBaseRow(key = "some-key", cells = Seq(
            HBaseCell(column = s"$ColumnFamily:col1-name", value = "col1-value"),
            HBaseCell(column = s"$ColumnFamily:col2-name", value = "col2-value")
          ))
        )
      }

      "when it represents multiple rows" in new Fixture {
        val jsonStr =
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

        read(jsonStr) shouldBe Seq(
          HBaseRow(key = "row1-key", cells = Seq(
            HBaseCell(column = s"$ColumnFamily:row1-col1-name", value = "row1-col1-value"),
            HBaseCell(column = s"$ColumnFamily:row1-col2-name", value = "row1-col2-value")
          )),
          HBaseRow(key = "row2-key", cells = Seq(
            HBaseCell(column = s"$ColumnFamily:row2-col1-name", value = "row2-col1-value"),
            HBaseCell(column = s"$ColumnFamily:row2-col2-name", value = "row2-col2-value")
          ))
        )
      }
    }

    "can be successfully written" - {
      /*
       * Used when inserting a single value, for example.
       */
      "when it represents a single value (cell)" in new Fixture {
        val data = Seq(HBaseRow(key = "row1-key", cells = Seq(
          HBaseCell(column = s"$ColumnFamily:row1-col1-name", value = "row1-col1-value")
        )))
        val expectedJsonStr =
          s"""|{"Row":[
              |  {"key":"${hbaseEncode("row1-key")}",
              |   "Cell":[
              |     {"column":"${hbaseEncode(s"$ColumnFamily:row1-col1-name")}",
              |      "$$":"${hbaseEncode("row1-col1-value")}"}
              |   ]}
              |]}""".stripMargin

        write(data) shouldBe Json.parse(expectedJsonStr)
      }

      /*
       * This is used when performing a "check and update", for example.
       * Note that this implies that a client must be able to define a row having non-unique column names.
       */
      "when it represents multiple values for a single (cell)" in new Fixture {
        val data = Seq(HBaseRow(key = "row1-key", cells = Seq(
          HBaseCell(column = s"$ColumnFamily:row1-col1-name", value = "row1-col1-new-value"),
          HBaseCell(column = s"$ColumnFamily:row1-col1-name", value = "row1-col1-old-value")
        )))
        val expectedJsonStr =
          s"""|{"Row":[
              |  {"key":"${hbaseEncode("row1-key")}",
              |   "Cell":[
              |     {"column":"${hbaseEncode(s"$ColumnFamily:row1-col1-name")}",
              |      "$$":"${hbaseEncode("row1-col1-new-value")}"},
              |      {"column":"${hbaseEncode(s"$ColumnFamily:row1-col1-name")}",
              |      "$$":"${hbaseEncode("row1-col1-old-value")}"}
              |   ]}
              |]}""".stripMargin

        write(data) shouldBe Json.parse(expectedJsonStr)
      }
    }
  }

  "A HBaseCell" - {
    "can be created from a columnName, fieldValue tuple" in new Fixture {
      val qualifier = "qualifier"
      val column = Column(ColumnFamily, qualifier)
      val value = "some-value"

      HBaseCell.fromField(column -> value) shouldBe HBaseCell(s"$ColumnFamily:$qualifier", value)
    }
  }
}
