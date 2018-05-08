//package repository.hbase.unitlinks
//
//import scala.concurrent.Future
//
//import com.google.inject.Inject
//import com.typesafe.scalalogging.LazyLogging
//
//import uk.gov.ons.sbr.models.Period
//import uk.gov.ons.sbr.models.unitlinks.{ UnitLinks, UnitType }
//
//import repository.RestRepository.{ ErrorMessage, Row, RowKey }
//import repository.hbase.enterprise.HBaseRestEnterpriseUnitRepositoryConfig
//import repository.{ RestRepository, RowMapper, UnitLinksRepository }
//import repository.hbase.HBase.LinksColumnFamily
//
//case class HBaseRestUnitLinksRepositoryConfig(tableName: String)
//
//class HBaseRestUnitLinksRepository @Inject() (
//    restRepository: RestRepository,
//    config: HBaseRestEnterpriseUnitRepositoryConfig,
//    rowMapper: RowMapper[UnitLinks]
//) extends UnitLinksRepository with LazyLogging {
//
//  override def retrieveUnitLinks(id: String, unitType: UnitType, period: Period): Future[Either[ErrorMessage, Option[UnitLinks]]] = {
//    logger.info(s"Retrieving UnitLinks with [$id] of [$unitType] for [$period]")
//    restRepository.findRecord(config.tableName, UnitLinksRowKey(id, unitType, period), LinksColumnFamily).map(fromErrorOrRow)
//  }
//
//  private def fromErrorOrRow(errorOrRow: Either[ErrorMessage, Option[(RowKey, Row)]]): Either[ErrorMessage, Option[UnitLinks]] = {
//    logger.debug(s"Unit Links response is [$errorOrRow]")
//    errorOrRow.right.flatMap { optRowKeyAndRow =>
//      optRowKeyAndRow.map(fromRow).fold[Either[ErrorMessage, Option[UnitLinks]]](Right(None)) { errorOrUnitLinks =>
//        logger.debug(s"From Row to Unit Links conversion is [$errorOrUnitLinks]")
//        errorOrUnitLinks.right.map(Some(_))
//      }
//    }
//  }
//
//  private def fromRow(row: (RowKey, Row)): Either[ErrorMessage, UnitLinks] = {
//    val optUnitLinks = rowMapper.fromRow(row)
//    if (optUnitLinks.isEmpty) logger.warn(s"Unable to construct Unit Links from HBase [${config.tableName}] of [$row]")
//    optUnitLinks.toRight("Unable to create Unit Links from row")
//  }
//
//}
