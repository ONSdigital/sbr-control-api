package repository.hbase

import java.nio.charset.StandardCharsets.UTF_8
import java.util.Base64

import play.api.libs.json.{ JsResult, JsValue, Json, Reads }

import repository.RestRepository.{ Row, RowKey }

object HBaseResponseReader extends HBaseResponseReaderMaker {

  private case class EncodedResult(Row: Seq[EncodedRow])
  private case class EncodedRow(key: RowKey, Cell: Seq[EncodedColumn])
  private case class EncodedColumn(column: String, `$`: String)

  private implicit val readsEncodedColumn = Json.reads[EncodedColumn]
  private implicit val readsEncodedRow = Json.reads[EncodedRow]
  private implicit val readsEncodedResult = Json.reads[EncodedResult]

  override def forColumnFamily(columnFamily: String): Reads[Seq[Row]] =
    new Reads[Seq[Row]] {
      override def reads(json: JsValue): JsResult[Seq[Row]] =
        json.validate[EncodedResult].map(_.Row.map(decodeRow(columnFamily)))
    }

  private def decodeRow(columnFamily: String)(encodedRow: EncodedRow) =
    Row(
      rowKey = decode(encodedRow.key),
      fields = encodedRow.Cell.map(decodeColumn).toMap.map {
        case (columnName, columnValue) => removeColumnFamilyFromColumnName(columnFamily, columnName) -> columnValue
      }
    )

  private def removeColumnFamilyFromColumnName(columnFamily: String, columnName: String): String = {
    val prefix = columnFamily + ":"
    columnName.replaceFirst(prefix, "")
  }

  private def decodeColumn(encodedColumn: EncodedColumn): (String, String) =
    decode(encodedColumn.column) -> decode(encodedColumn.`$`)

  private def decode(value: String): String =
    new String(Base64.getDecoder.decode(value), UTF_8.name())
}