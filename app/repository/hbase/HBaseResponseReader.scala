package repository.hbase

import java.nio.charset.StandardCharsets.UTF_8
import java.util.Base64

import play.api.libs.json.{ JsResult, JsValue, Json, Reads }
import repository.RestRepository.Row

object HBaseResponseReader extends HBaseResponseReaderMaker {

  private case class EncodedResult(Row: Seq[EncodedRow])
  private case class EncodedRow(Cell: Seq[EncodedColumn])
  private case class EncodedColumn(column: String, `$`: String)

  private implicit val readsEncodedColumn = Json.reads[EncodedColumn]
  private implicit val readsEncodedRow = Json.reads[EncodedRow]
  private implicit val readsEncodedResult = Json.reads[EncodedResult]

  override def forColumnGroup(columnGroup: String): Reads[Seq[Row]] =
    new Reads[Seq[Row]] {
      override def reads(json: JsValue): JsResult[Seq[Row]] =
        json.validate[EncodedResult].map(_.Row.map(decodeRow(columnGroup)))
    }

  private def decodeRow(columnGroup: String)(encodedRow: EncodedRow): Row =
    encodedRow.Cell.map(decodeColumn).toMap.map {
      case (columnName, columnValue) => removeColumnGroupFromColumnName(columnGroup, columnName) -> columnValue
    }

  private def removeColumnGroupFromColumnName(columnGroup: String, columnName: String): String = {
    val prefix = columnGroup + ":"
    columnName.replaceFirst(prefix, "")
  }

  private def decodeColumn(encodedColumn: EncodedColumn): (String, String) =
    decode(encodedColumn.column) -> decode(encodedColumn.`$`)

  private def decode(value: String): String =
    new String(Base64.getDecoder.decode(value), UTF_8.name())
}