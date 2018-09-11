package repository.hbase

import repository.RestRepository.{ Field, RowKey }
import repository.hbase.HBaseData.{ HBaseCell, HBaseRow }

object CreateOrReplace {
  def apply(rowKey: RowKey, field: Field): Seq[HBaseRow] =
    Seq(HBaseRow(key = rowKey, cells = Seq(
      HBaseCell.fromField(field)
    )))
}
