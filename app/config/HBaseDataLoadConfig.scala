package config

import java.io.File

import uk.gov.ons.sbr.data.domain.UnitType

/**
 * Created by haqa on 31/08/2017.
 */
trait HBaseDataLoadConfig {

  // Setting data for first period (201706)
  // Enterprise Data
  val firstPeriod = "201706"
  val secondPeriod = "201708"
  val entData201706: List[String] = List[String](UnitType.ENTERPRISE.toString, firstPeriod, new File("conf/sample/201706/sbr-2500-ent-data.csv").toURI.toURL.toExternalForm)

  // ENT ~ VAT/PAYE/CH/LEU Links
  val entLeu201706: List[String] = List[String](UnitType.ENTERPRISE.toString + "~"
    + UnitType.LEGAL_UNIT.toString, firstPeriod, new File("conf/sample/201706/sbr-2500-ent-leu-links.csv").toURI.toURL.toExternalForm)
  val entVat201706: List[String] = List[String](UnitType.ENTERPRISE.toString + "~"
    + UnitType.VAT.toString, firstPeriod, new File("conf/sample/201706/sbr-2500-ent-vat-links.csv").toURI.toURL.toExternalForm)
  val entPaye201706: List[String] = List[String](UnitType.ENTERPRISE.toString + "~"
    + UnitType.PAYE.toString, firstPeriod, new File("conf/sample/201706/sbr-2500-ent-paye-links.csv").toURI.toURL.toExternalForm)
  val entCh201706: List[String] = List[String](UnitType.ENTERPRISE.toString + "~"
    + UnitType.COMPANY_REGISTRATION.toString, firstPeriod, new File("conf/sample/201706/sbr-2500-ent-ch-links.csv").toURI.toURL.toExternalForm)

  // LEU ~ VAT/PAYE/CH Links
  val leuCh201706: List[String] = List[String](UnitType.LEGAL_UNIT.toString + "~"
    + UnitType.COMPANY_REGISTRATION.toString, firstPeriod, new File("conf/sample/201706/sbr-2500-leu-ch-links.csv").toURI.toURL.toExternalForm)
  val leuPaye201706: List[String] = List[String](UnitType.LEGAL_UNIT.toString + "~"
    + UnitType.PAYE.toString, firstPeriod, new File("conf/sample/201706/sbr-2500-leu-paye-links.csv").toURI.toURL.toExternalForm)
  val leuVat201706: List[String] = List[String](UnitType.LEGAL_UNIT.toString + "~"
    + UnitType.VAT.toString, firstPeriod, new File("conf/sample/201706/sbr-2500-leu-vat-links.csv").toURI.toURL.toExternalForm)

  // Setting data for second period (201708)
  // Enterprise Data
  val entData201708: List[String] = List[String](UnitType.ENTERPRISE.toString, secondPeriod, new File("conf/sample/201708/sbr-2500-ent-data.csv").toURI.toURL.toExternalForm)

  // ENT ~ VAT/PAYE/CH/LEU Links
  val entLeu201708: List[String] = List[String](UnitType.ENTERPRISE.toString + "~"
    + UnitType.LEGAL_UNIT.toString, secondPeriod, new File("conf/sample/201708/sbr-2500-ent-leu-links.csv").toURI.toURL.toExternalForm)
  val entVat201708: List[String] = List[String](UnitType.ENTERPRISE.toString + "~"
    + UnitType.VAT.toString, secondPeriod, new File("conf/sample/201708/sbr-2500-ent-vat-links.csv").toURI.toURL.toExternalForm)
  val entPaye201708: List[String] = List[String](UnitType.ENTERPRISE.toString + "~"
    + UnitType.PAYE.toString, secondPeriod, new File("conf/sample/201708/sbr-2500-ent-paye-links.csv").toURI.toURL.toExternalForm)
  val entCh201708: List[String] = List[String](UnitType.ENTERPRISE.toString + "~"
    + UnitType.COMPANY_REGISTRATION.toString, secondPeriod, new File("conf/sample/201708/sbr-2500-ent-ch-links.csv").toURI.toURL.toExternalForm)

  // LEU ~ VAT/PAYE/CH Links
  val leuCh201708: List[String] = List[String](UnitType.LEGAL_UNIT.toString + "~"
    + UnitType.COMPANY_REGISTRATION.toString, secondPeriod, new File("conf/sample/201708/sbr-2500-leu-ch-links.csv").toURI.toURL.toExternalForm)
  val leuPaye201708: List[String] = List[String](UnitType.LEGAL_UNIT.toString + "~"
    + UnitType.PAYE.toString, secondPeriod, new File("conf/sample/201708/sbr-2500-leu-paye-links.csv").toURI.toURL.toExternalForm)
  val leuVat201708: List[String] = List[String](UnitType.LEGAL_UNIT.toString + "~"
    + UnitType.VAT.toString, secondPeriod, new File("conf/sample/201708/sbr-2500-leu-vat-links.csv").toURI.toURL.toExternalForm)
}
