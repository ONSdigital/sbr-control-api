package services

import javax.inject.Inject

import org.apache.hadoop.util.ToolRunner
import play.api.Configuration
import uk.gov.ons.sbr.data.hbase.HBaseConnector
import uk.gov.ons.sbr.data.hbase.load.BulkLoader

/**
 * Created by coolit on 13/02/2018.
 */
class HBaseDataLoader @Inject() (val configuration: Configuration) extends HBaseDataLoadConfig {
  val dbConf = "asd"

  if (true) {
    val bulkLoader = new BulkLoader()
    //if (hbaseRest) HBaseConnector.getInstance().connect() // Need this for connecting to local HBase REST

    //   Load in data for first period (201706)
    ToolRunner.run(HBaseConnector.getInstance().getConfiguration, bulkLoader, entData201706.toArray)

    // Load in Links
    ToolRunner.run(HBaseConnector.getInstance().getConfiguration, bulkLoader, entLeu201706.toArray)
    ToolRunner.run(HBaseConnector.getInstance().getConfiguration, bulkLoader, entVat201706.toArray)
    ToolRunner.run(HBaseConnector.getInstance().getConfiguration, bulkLoader, entPaye201706.toArray)
    ToolRunner.run(HBaseConnector.getInstance().getConfiguration, bulkLoader, entCh201706.toArray)
    ToolRunner.run(HBaseConnector.getInstance().getConfiguration, bulkLoader, leuCh201706.toArray)
    ToolRunner.run(HBaseConnector.getInstance().getConfiguration, bulkLoader, leuPaye201706.toArray)
    ToolRunner.run(HBaseConnector.getInstance().getConfiguration, bulkLoader, leuVat201706.toArray)

    // Load in data for second period (201708)
    ToolRunner.run(HBaseConnector.getInstance().getConfiguration, bulkLoader, entData201708.toArray)

    // Load in Links
    ToolRunner.run(HBaseConnector.getInstance().getConfiguration, bulkLoader, entLeu201708.toArray)
    ToolRunner.run(HBaseConnector.getInstance().getConfiguration, bulkLoader, entVat201708.toArray)
    ToolRunner.run(HBaseConnector.getInstance().getConfiguration, bulkLoader, entPaye201708.toArray)
    ToolRunner.run(HBaseConnector.getInstance().getConfiguration, bulkLoader, entCh201708.toArray)
    ToolRunner.run(HBaseConnector.getInstance().getConfiguration, bulkLoader, leuCh201708.toArray)
    ToolRunner.run(HBaseConnector.getInstance().getConfiguration, bulkLoader, leuPaye201708.toArray)
    ToolRunner.run(HBaseConnector.getInstance().getConfiguration, bulkLoader, leuVat201708.toArray)
  }
}
