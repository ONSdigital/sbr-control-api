package repository.hbase

import repository.RestRepository.{ Field, RowKey }
import repository.hbase.HBaseData.{ HBaseCell, HBaseRow }

private[hbase] object CheckAndUpdate {
  /*
   * The order of the cells is important - "target value" followed by "check value".
   */
  def apply(rowKey: RowKey, beforeField: Field, afterField: Field): Seq[HBaseRow] =
    Seq(HBaseRow(key = rowKey, cells = Seq(
      asHBaseCell(afterField),
      asHBaseCell(beforeField)
    )))

  private def asHBaseCell(field: Field): HBaseCell =
    HBaseCell(column = field._1, value = field._2)
}
