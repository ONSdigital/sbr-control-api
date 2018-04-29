package repository.hbase

object HBase {
  val RowKeyDelimiter = "~"
  val Wildcard = "*"
  val DefaultColumnFamily = "d"

  val unitChildPrefix = "c_"
  val unitParentPrefix = "p_"

  def rowKeyUrl(protocolWithHostname: String, port: String, namespace: String, table: String, rowKey: String, columnFamily: String): String =
    s"$protocolWithHostname:$port/${rowKeyUrl(namespace, table, rowKey, columnFamily)}"

  def rowKeyUrl(namespace: String, table: String, rowKey: String, columnFamily: String): String =
    s"$namespace:$table/$rowKey/$columnFamily"
}
