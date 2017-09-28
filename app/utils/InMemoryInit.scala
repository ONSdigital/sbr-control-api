package utils

import java.io.File

import org.apache.hadoop.util.ToolRunner
import play.api.Logger

import uk.gov.ons.sbr.data.domain.UnitType
import uk.gov.ons.sbr.data.hbase.HBaseConnector
import uk.gov.ons.sbr.data.hbase.load.BulkLoader

/**
 * Created by haqa on 31/08/2017.
 */
object InMemoryInit {

  Logger.info("Loading local CSVs into In-Memory HBase...")
  protected val bulkLoader = new BulkLoader()

  // Setting data for first period (201706)
  // Enterprise Data
  val firstPeriod = "201706"
  val secondPeriod = "201708"
  protected val entData201706: List[String] = List[String](UnitType.ENTERPRISE.toString, firstPeriod, "conf/sample/201706/sbr-2500-ent-data.csv")

  // ENT ~ VAT/PAYE/CH/LEU Links
  protected val entLeu201706: List[String] = List[String](UnitType.ENTERPRISE.toString + "~"
    + UnitType.LEGAL_UNIT.toString, firstPeriod, "conf/sample/201706/sbr-2500-ent-leu-links.csv")
  protected val entVat201706: List[String] = List[String](UnitType.ENTERPRISE.toString + "~"
    + UnitType.VAT.toString, firstPeriod, "conf/sample/201706/sbr-2500-ent-vat-links.csv")
  protected val entPaye201706: List[String] = List[String](UnitType.ENTERPRISE.toString + "~"
    + UnitType.PAYE.toString, firstPeriod, "conf/sample/201706/sbr-2500-ent-paye-links.csv")
  protected val entCh201706: List[String] = List[String](UnitType.ENTERPRISE.toString + "~"
    + UnitType.COMPANY_REGISTRATION.toString, firstPeriod, "conf/sample/201706/sbr-2500-ent-ch-links.csv")

  // LEU ~ VAT/PAYE/CH Links
  protected val leuCh201706: List[String] = List[String](UnitType.LEGAL_UNIT.toString + "~"
    + UnitType.COMPANY_REGISTRATION.toString, firstPeriod, "conf/sample/201706/sbr-2500-leu-ch-links.csv")
  protected val leuPaye201706: List[String] = List[String](UnitType.LEGAL_UNIT.toString + "~"
    + UnitType.PAYE.toString, firstPeriod, "conf/sample/201706/sbr-2500-leu-paye-links.csv")
  protected val leuVat201706: List[String] = List[String](UnitType.LEGAL_UNIT.toString + "~"
    + UnitType.VAT.toString, firstPeriod, "conf/sample/201706/sbr-2500-leu-vat-links.csv")

  // Setting data for second period (201708)
  // Enterprise Data
  protected val entData201708: List[String] = List[String](UnitType.ENTERPRISE.toString, secondPeriod, "conf/sample/201708/sbr-2500-ent-data.csv")

  // ENT ~ VAT/PAYE/CH/LEU Links
  protected val entLeu201708: List[String] = List[String](UnitType.ENTERPRISE.toString + "~"
    + UnitType.LEGAL_UNIT.toString, secondPeriod, "conf/sample/201708/sbr-2500-ent-leu-links.csv")
  protected val entVat201708: List[String] = List[String](UnitType.ENTERPRISE.toString + "~"
    + UnitType.VAT.toString, secondPeriod, "conf/sample/201708/sbr-2500-ent-vat-links.csv")
  protected val entPaye201708: List[String] = List[String](UnitType.ENTERPRISE.toString + "~"
    + UnitType.PAYE.toString, secondPeriod, "conf/sample/201708/sbr-2500-ent-paye-links.csv")
  protected val entCh201708: List[String] = List[String](UnitType.ENTERPRISE.toString + "~"
    + UnitType.COMPANY_REGISTRATION.toString, secondPeriod, "conf/sample/201708/sbr-2500-ent-ch-links.csv")

  // LEU ~ VAT/PAYE/CH Links
  protected val leuCh201708: List[String] = List[String](UnitType.LEGAL_UNIT.toString + "~"
    + UnitType.COMPANY_REGISTRATION.toString, secondPeriod, "conf/sample/201708/sbr-2500-leu-ch-links.csv")
  protected val leuPaye201708: List[String] = List[String](UnitType.LEGAL_UNIT.toString + "~"
    + UnitType.PAYE.toString, secondPeriod, "conf/sample/201708/sbr-2500-leu-paye-links.csv")
  protected val leuVat201708: List[String] = List[String](UnitType.LEGAL_UNIT.toString + "~"
    + UnitType.VAT.toString, secondPeriod, "conf/sample/201708/sbr-2500-leu-vat-links.csv")

  // Load in data for first period (201706)
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
