package repository.hbase

import utils.BaseUrl

object HBase {
  val RowKeyDelimiter = "~"
  val Wildcard = "*"
  val DefaultColumnFamily = "d"

  def rowKeyUrl(withBase: BaseUrl, namespace: String, table: String, rowKey: String, columnFamily: String): String =
    s"${BaseUrl.asUrlString(withBase)}/${rowKeyUrl(namespace, table, rowKey, columnFamily)}"

  def rowKeyUrl(namespace: String, table: String, rowKey: String, columnFamily: String): String =
    s"$namespace:$table/$rowKey/$columnFamily"
}
