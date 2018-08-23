package repository.hbase

import play.api.libs.json.{ JsResult, JsValue, Reads }
import repository.RestRepository.Row
import repository.hbase.HBaseData.{ HBaseCell, HBaseRow }

object HBaseResponseReader extends HBaseResponseReaderMaker {
  override def forColumnFamily(columnFamily: String): Reads[Seq[Row]] =
    new Reads[Seq[Row]] {
      override def reads(json: JsValue): JsResult[Seq[Row]] = {
        val convertingCellToField = toField(columnFamily) _
        val hbaseRowsResult = json.validate[Seq[HBaseRow]](HBaseData.format)
        hbaseRowsResult.map(_.map(toRow(convertingCellToField)))
      }
    }

  private def toRow(toField: HBaseCell => (String, String))(hbaseRow: HBaseRow): Row =
    Row(hbaseRow.key, hbaseRow.cells.map(toField).toMap)

  private def toField(columnFamily: String)(cell: HBaseCell): (String, String) =
    removeColumnFamilyFromColumnName(columnFamily, cell.column) -> cell.value

  private def removeColumnFamilyFromColumnName(columnFamily: String, columnName: String): String = {
    val prefix = columnFamily + ":"
    columnName.replaceFirst(prefix, "")
  }
}