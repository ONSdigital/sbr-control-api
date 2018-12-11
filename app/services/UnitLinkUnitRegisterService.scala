package services
import javax.inject.Inject
import repository.RestRepository
import repository.hbase.PeriodTableName
import repository.hbase.unitlinks.HBaseRestUnitLinksRepository.ColumnFamily
import repository.hbase.unitlinks.{HBaseRestUnitLinksRepositoryConfig, UnitLinksRowKey}
import uk.gov.ons.sbr.models.{Period, UnitKey}

import scala.concurrent.{ExecutionContext, Future}

/*
 * This service abstracts over how we determine whether or not a (non-admin) unit exists in the register.
 *
 * In this implementation, we simply check that it exists in the unit links table.
 * It would be preferable to guarantee consistency and also check the relevant unit table.
 *
 * Note that we are looking up data in the unit links table directly here, rather than using the appropriate
 * repository.  This does introduce some duplication with the UnitLinksRepository implementation.  However,
 * it is more efficient because we only care whether or not a row exists, and do not need to bind the row to
 * a UnitLinks model.  It also allows us to use this implementation from within the UnitLinksRepository
 * implementation itself, giving us a consistent approach to checking for the existence of a unit.  By contrast,
 * attempting to inject an instance of a UnitRegisterService that used the UnitLinksRepository would create a
 * cyclic dependency.
 *
 * Note also that we cannot use a HEAD request here for efficiency, as Cloudera returns 200 OK with an empty
 * Json document when a key is not found.  We therefore have to inspect the body to make the correct determination.
 */
class UnitLinkUnitRegisterService @Inject() (
    restRepository: RestRepository,
    config: HBaseRestUnitLinksRepositoryConfig)(implicit ec: ExecutionContext) extends UnitRegisterService {
  override def isRegisteredUnit(unitKey: UnitKey): Future[UnitRegisterResult] = {
    restRepository.findRow(tableName(unitKey.period), UnitLinksRowKey(unitKey.unitId, unitKey.unitType), ColumnFamily).map {
      _.fold(
        errorMessage => UnitRegisterFailure(errorMessage),
        optRow => optRow.fold[UnitRegisterResult](UnitNotFound)(_ => UnitFound)
      )
    }
  }

  private def tableName(period: Period): String =
    PeriodTableName(config.tableName, period)
}
