package support

trait HBaseResponseFixture {

  val hbaseEncode: String => String =
    Base64.encode

  def aRowWith(key: String, columns: String*): String =
    s"""|{"key": "${hbaseEncode(key)}",
        | "Cell": ${columns.mkString("[", ",", "]")}
        |}""".stripMargin

  def aColumnWith(name: String, value: String, timestamp: Long = 1520333985745L): String =
    s"""|{"column": "${hbaseEncode(name)}",
        | "timestamp": $timestamp,
        | "$$": "${hbaseEncode(value)}"
        |}""".stripMargin
}
