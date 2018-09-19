package repository.hbase

import java.nio.charset.StandardCharsets
import java.util.Base64

import play.api.libs.json.{ Format, JsResult, JsValue, Json }
import repository.RestRepository.Field

/*
 * HBase only stores bytes, and so all data sent to / read from HBase is Base64 encoded.
 * We shield the client from this, and provide a format that seamlessly decodes / encodes as required.
 *
 * Note that our read API (see RESTRepository) modelled fields as a Map, making the assumption that the
 * fields in a row would have unique names.  This abstraction unfortunately does not hold for write operations,
 * where checkAndAct operations require us to construct a json document containing a "row" with two versions of
 * the same field - the before & after values.  HBaseResponseReader is used for the read API, and uses this
 * format to parse the HBase response before subsequently adapting it to expose the definition of a Row expected
 * by that API, rather than the HBaseRow defined here.
 */
object HBaseData {
  case class HBaseRow(key: String, cells: Seq[HBaseCell])
  case class HBaseCell(column: String, value: String)

  object HBaseCell {
    val fromField: ((String, String)) => HBaseCell =
      (apply _).tupled

    def fromField(field: Field): HBaseCell =
      HBaseCell(column = Column.name(field._1), value = field._2)
  }

  /*
   * These types model the underlying HBase representation.
   * As we are relying on automatic format derivation, the field names must match those used by HBase exactly.
   */
  private case class EncodedRows(Row: Seq[EncodedRow])
  private case class EncodedRow(key: String, Cell: Seq[EncodedColumn])
  private case class EncodedColumn(column: String, `$`: String)

  private implicit val encodedColumnFormat = Json.format[EncodedColumn]
  private implicit val encodedRowFormat = Json.format[EncodedRow]
  private implicit val encodedRowsFormat = Json.format[EncodedRows]

  private val Charset = StandardCharsets.UTF_8

  private object Decoder extends ((EncodedRow) => HBaseRow) {
    def apply(encodedRow: EncodedRow): HBaseRow =
      HBaseRow(key = decode(encodedRow.key), cells = encodedRow.Cell.map(decodeColumn))

    private def decodeColumn(encodedColumn: EncodedColumn): HBaseCell =
      HBaseCell(column = decode(encodedColumn.column), value = decode(encodedColumn.`$`))

    private def decode(value: String): String = {
      val bytes = Base64.getDecoder.decode(value)
      new String(bytes, Charset)
    }
  }

  private object Encoder extends ((HBaseRow) => EncodedRow) {
    def apply(row: HBaseRow): EncodedRow =
      EncodedRow(key = encode(row.key), Cell = row.cells.map(encodeColumn))

    private def encodeColumn(col: HBaseCell): EncodedColumn =
      EncodedColumn(column = encode(col.column), `$` = encode(col.value))

    // TODO HBase does accept this, but consider if getEncoder.withoutPadding() would be preferable ...
    private def encode(value: String): String = {
      val bytes = value.getBytes(Charset)
      Base64.getEncoder.encodeToString(bytes)
    }
  }

  implicit val format: Format[Seq[HBaseRow]] = new Format[Seq[HBaseRow]] {
    override def reads(json: JsValue): JsResult[Seq[HBaseRow]] =
      json.validate[EncodedRows].map {
        _.Row.map(Decoder)
      }

    override def writes(rows: Seq[HBaseRow]): JsValue = {
      val encodedRows = EncodedRows(rows.map(Encoder))
      Json.toJson(encodedRows)
    }
  }
}
