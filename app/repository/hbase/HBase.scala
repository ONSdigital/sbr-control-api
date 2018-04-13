package repository.hbase

object HBase {
  val RowKeyDelimiter = "~"
  val DefaultColumnGroup = "d"

  def rowKeyUrl(protocolWithHostname: String, port: String, namespace: String, table: String, rowKey: String, columnGroup: String): String =
    s"$protocolWithHostname:$port/${rowKeyUrl(namespace, table, rowKey, columnGroup)}"

  def rowKeyUrl(namespace: String, table: String, rowKey: String, columnGroup: String): String =
    s"$namespace:$table/$rowKey/$columnGroup"
}
