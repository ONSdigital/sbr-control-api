package Services

import org.apache.hadoop.util.ToolRunner
import play.api.Logger
import uk.gov.ons.sbr.data.domain.UnitType
import uk.gov.ons.sbr.data.hbase.HBaseConnector
import uk.gov.ons.sbr.data.hbase.load.BulkLoader

/**
 * Created by haqa on 31/08/2017.
 */
object HBaseInMemoryConfig extends DBConnector {

  Logger.info("Loading local CSVs into In-Memory HBase...")
  private val bulkLoader = new BulkLoader()

  // Enterprise Data
  private val argsData: List[String] = List[String](UnitType.ENTERPRISE.toString, "201706", "conf/sample/sbr-2500-ent-data.csv")

  // ENT ~ VAT/PAYE/CH/LEU Links
  private val argsLinksEntLeu: List[String] = List[String](UnitType.ENTERPRISE.toString + "~"
    + UnitType.LEGAL_UNIT.toString, "201706", "conf/sample/sbr-2500-ent-leu-links.csv")
  private val argsLinksEntVat: List[String] = List[String](UnitType.ENTERPRISE.toString + "~"
    + UnitType.VAT.toString, "201706", "conf/sample/sbr-2500-ent-vat-links.csv")
  private val argsLinksEntPaye: List[String] = List[String](UnitType.ENTERPRISE.toString + "~"
    + UnitType.PAYE.toString, "201706", "conf/sample/sbr-2500-ent-paye-links.csv")
  private val argsLinksEntCh: List[String] = List[String](UnitType.ENTERPRISE.toString + "~"
    + UnitType.COMPANY_REGISTRATION.toString, "201706", "conf/sample/sbr-2500-ent-ch-links.csv")

  // LEU ~ VAT/PAYE/CH Links
  private val argsLinksCh: List[String] = List[String](UnitType.LEGAL_UNIT.toString + "~"
    + UnitType.COMPANY_REGISTRATION.toString, "201706", "conf/sample/sbr-2500-leu-ch-links.csv")
  private val argsLinksPaye: List[String] = List[String](UnitType.LEGAL_UNIT.toString + "~"
    + UnitType.PAYE.toString, "201706", "conf/sample/sbr-2500-leu-paye-links.csv")
  private val argsLinksVat: List[String] = List[String](UnitType.LEGAL_UNIT.toString + "~"
    + UnitType.VAT.toString, "201706", "conf/sample/sbr-2500-leu-vat-links.csv")

  // Load in data
  ToolRunner.run(HBaseConnector.getInstance().getConfiguration, bulkLoader, argsData.toArray)

  // Load in Links
  ToolRunner.run(HBaseConnector.getInstance().getConfiguration, bulkLoader, argsLinksEntLeu.toArray)
  ToolRunner.run(HBaseConnector.getInstance().getConfiguration, bulkLoader, argsLinksEntVat.toArray)
  ToolRunner.run(HBaseConnector.getInstance().getConfiguration, bulkLoader, argsLinksEntPaye.toArray)
  ToolRunner.run(HBaseConnector.getInstance().getConfiguration, bulkLoader, argsLinksEntCh.toArray)
  ToolRunner.run(HBaseConnector.getInstance().getConfiguration, bulkLoader, argsLinksCh.toArray)
  ToolRunner.run(HBaseConnector.getInstance().getConfiguration, bulkLoader, argsLinksPaye.toArray)
  ToolRunner.run(HBaseConnector.getInstance().getConfiguration, bulkLoader, argsLinksVat.toArray)

}
