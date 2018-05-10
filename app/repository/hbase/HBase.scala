package repository.hbase

object HBase {
  val RowKeyDelimiter = "~"
  val Wildcard = "*"
  val UnitColumnFamily = "d"
  val LinksColumnFamily = "l"

  val UnitChildPrefix = "c_"
  val UnitParentPrefix = "p_"
  val ChildOrParentPrefixLength = 2

  def rowKeyUrl(protocolWithHostname: String, port: String, namespace: String, table: String, rowKey: String, columnFamily: String): String =
    s"$protocolWithHostname:$port/${rowKeyUrl(namespace, table, rowKey, columnFamily)}"

  def rowKeyUrl(namespace: String, table: String, rowKey: String, columnFamily: String): String =
    s"$namespace:$table/$rowKey/$columnFamily"
}
