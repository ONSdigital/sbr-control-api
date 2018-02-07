package utils

import com.typesafe.config.Config
import org.apache.hadoop.hbase.TableName
import play.api.Configuration

/**
 * Created by coolit on 05/02/2018.
 */
trait HBaseConfig {

  implicit val configuration: Configuration
  private val hBaseConfig: Config = configuration.underlying.getConfig("db")
  println(s"new conf is: ${hBaseConfig}")

  //    private val nameSpace: String = if (hBaseConfig.getBoolean("initialize")) {
  //      hBaseConfig.getString("in.memory.namespace")
  //    } else { hBaseConfig.getString("namespace") }

  private val namespace: String = "sbr_control_api" // hBaseConfig.getString("namespace")

  lazy val tableName: TableName = TableName.valueOf(
    namespace,
    "enterprise" // was table.name
  )

  lazy val unitTableName: TableName = TableName.valueOf(
    namespace,
    "unit_links"
  )

  lazy val username: String = "" // hBaseConfig.getString("username")
  lazy val password: String = "" // hBaseConfig.getString("password")
  lazy val baseUrl: String = "http://localhost:8080" // hBaseConfig.getString("rest.endpoint")
  lazy val columnFamily: String = "d" // hBaseConfig.getString("column.family")
}
