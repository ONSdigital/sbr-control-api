package services

import java.io.File

import org.apache.hadoop.util.ToolRunner
import play.api.Logger

import uk.gov.ons.sbr.data.domain.UnitType
import uk.gov.ons.sbr.data.hbase.HBaseConnector
import uk.gov.ons.sbr.data.hbase.load.BulkLoader

/**
 * Created by haqa on 31/08/2017.
 */
object HBaseInMemoryConfig {

  Logger.info("Loading local CSVs into In-Memory HBase...")
  private val bulkLoader = new BulkLoader()

  // Setting data for first period (201706)
  // Enterprise Data
  val firstPeriod = "201706"
  val secondPeriod = "201708"
  private val entData201706: List[String] = List[String](UnitType.ENTERPRISE.toString, firstPeriod, new File("conf/sample/201706/sbr-2500-ent-data.csv").toURI.toURL.toExternalForm)

  // ENT ~ VAT/PAYE/CH/LEU Links
  private val entLeu201706: List[String] = List[String](UnitType.ENTERPRISE.toString + "~"
    + UnitType.LEGAL_UNIT.toString, firstPeriod, new File("conf/sample/201706/sbr-2500-ent-leu-links.csv").toURI.toURL.toExternalForm)
  private val entVat201706: List[String] = List[String](UnitType.ENTERPRISE.toString + "~"
    + UnitType.VAT.toString, firstPeriod, new File("conf/sample/201706/sbr-2500-ent-vat-links.csv").toURI.toURL.toExternalForm)
  private val entPaye201706: List[String] = List[String](UnitType.ENTERPRISE.toString + "~"
    + UnitType.PAYE.toString, firstPeriod, new File("conf/sample/201706/sbr-2500-ent-paye-links.csv").toURI.toURL.toExternalForm)
  private val entCh201706: List[String] = List[String](UnitType.ENTERPRISE.toString + "~"
    + UnitType.COMPANY_REGISTRATION.toString, firstPeriod, new File("conf/sample/201706/sbr-2500-ent-ch-links.csv").toURI.toURL.toExternalForm)

  // LEU ~ VAT/PAYE/CH Links
  private val leuCh201706: List[String] = List[String](UnitType.LEGAL_UNIT.toString + "~"
    + UnitType.COMPANY_REGISTRATION.toString, firstPeriod, new File("conf/sample/201706/sbr-2500-leu-ch-links.csv").toURI.toURL.toExternalForm)
  private val leuPaye201706: List[String] = List[String](UnitType.LEGAL_UNIT.toString + "~"
    + UnitType.PAYE.toString, firstPeriod, new File("conf/sample/201706/sbr-2500-leu-paye-links.csv").toURI.toURL.toExternalForm)
  private val leuVat201706: List[String] = List[String](UnitType.LEGAL_UNIT.toString + "~"
    + UnitType.VAT.toString, firstPeriod, new File("conf/sample/201706/sbr-2500-leu-vat-links.csv").toURI.toURL.toExternalForm)

  // Setting data for second period (201708)
  // Enterprise Data
  private val entData201708: List[String] = List[String](UnitType.ENTERPRISE.toString, secondPeriod, new File("conf/sample/201708/sbr-2500-ent-data.csv").toURI.toURL.toExternalForm)

  // ENT ~ VAT/PAYE/CH/LEU Links
  private val entLeu201708: List[String] = List[String](UnitType.ENTERPRISE.toString + "~"
    + UnitType.LEGAL_UNIT.toString, secondPeriod, new File("conf/sample/201708/sbr-2500-ent-leu-links.csv").toURI.toURL.toExternalForm)
  private val entVat201708: List[String] = List[String](UnitType.ENTERPRISE.toString + "~"
    + UnitType.VAT.toString, secondPeriod, new File("conf/sample/201708/sbr-2500-ent-vat-links.csv").toURI.toURL.toExternalForm)
  private val entPaye201708: List[String] = List[String](UnitType.ENTERPRISE.toString + "~"
    + UnitType.PAYE.toString, secondPeriod, new File("conf/sample/201708/sbr-2500-ent-paye-links.csv").toURI.toURL.toExternalForm)
  private val entCh201708: List[String] = List[String](UnitType.ENTERPRISE.toString + "~"
    + UnitType.COMPANY_REGISTRATION.toString, secondPeriod, new File("conf/sample/201708/sbr-2500-ent-ch-links.csv").toURI.toURL.toExternalForm)

  // LEU ~ VAT/PAYE/CH Links
  private val leuCh201708: List[String] = List[String](UnitType.LEGAL_UNIT.toString + "~"
    + UnitType.COMPANY_REGISTRATION.toString, secondPeriod, new File("conf/sample/201708/sbr-2500-leu-ch-links.csv").toURI.toURL.toExternalForm)
  private val leuPaye201708: List[String] = List[String](UnitType.LEGAL_UNIT.toString + "~"
    + UnitType.PAYE.toString, secondPeriod, new File("conf/sample/201708/sbr-2500-leu-paye-links.csv").toURI.toURL.toExternalForm)
  private val leuVat201708: List[String] = List[String](UnitType.LEGAL_UNIT.toString + "~"
    + UnitType.VAT.toString, secondPeriod, new File("conf/sample/201708/sbr-2500-leu-vat-links.csv").toURI.toURL.toExternalForm)

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
