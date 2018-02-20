package config

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

  private val hbaseRestNameSpace: String = hBaseRestConfig.getString("namespace")

  lazy val enterpriseTableName: TableName = TableName.valueOf(
    hbaseRestNameSpace,
    hBaseRestConfig.getString("enterprise.table.name")
  )

  lazy val unitTableName: TableName = TableName.valueOf(
    hbaseRestNameSpace,
    hBaseRestConfig.getString("unit.links.table.name")
  )

  lazy val username: String = hBaseRestConfig.getString("username")
  lazy val password: String = hBaseRestConfig.getString("password")
  lazy val host: String = hBaseRestConfig.getString("host")
  lazy val port: String = hBaseRestConfig.getString("port")
  lazy val baseUrl: String = s"$host:$port"
  lazy val columnFamily: String = hBaseRestConfig.getString("column.family")
}
