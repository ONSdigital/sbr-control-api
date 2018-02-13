package utils

import com.typesafe.config.Config
import org.apache.hadoop.hbase.TableName
import play.api.Configuration

/**
 * Created by coolit on 05/02/2018.
 */
trait HBaseConfig {

  implicit val configuration: Configuration
  private val hBaseConfig: Config = configuration.underlying.getConfig("env.default.db")
  private val hBaseRestConfig: Config = hBaseConfig.getConfig("hbase-rest")

  //  private val nameSpace: String = if (hBaseConfig.getBoolean("init")) {
  //    hBaseConfig.getString("in.memory.namespace")
  //  } else { hBaseConfig.getString("namespace") }

  private val hbaseRestNameSpace: String = hBaseRestConfig.getString("namespace")

  lazy val enterpriseTableName: TableName = TableName.valueOf(
    hbaseRestNameSpace,
    "enterprise" // was table.name
  )

  lazy val unitTableName: TableName = TableName.valueOf(
    hbaseRestNameSpace,
    "unit_links"
  )

  lazy val username: String = "" // hBaseConfig.getString("username")
  lazy val password: String = "" // hBaseConfig.getString("password")
  lazy val baseUrl: String = "http://localhost:8080" // hBaseConfig.getString("rest.endpoint")
  lazy val columnFamily: String = "d" // hBaseConfig.getString("column.family")
}
