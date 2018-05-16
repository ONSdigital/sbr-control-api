package repository.hbase.legalunit

import scala.util.Try

import uk.gov.ons.sbr.models.enterprise.{ EnterpriseLink, Ern }
import uk.gov.ons.sbr.models.legalunit.{ LegalUnit, UBRN }

import repository.RestRepository.Row
import repository.RowMapper
import repository.hbase.legalunit.LegalUnitColumns._

/*
 * The following fields are optional:
 * - luref
 * - entref
 * - tradingstyle
 * - address2
 * - address3
 * - address4
 * - address5
 *
 * Note that we use '=' for these fields within the body of the for expression rather than a generator '<-', so
 * that we capture the field as an Option.
 */
object LegalUnitRowMapper extends RowMapper[LegalUnit] {

  override def fromRow(variables: Row): Option[LegalUnit] =
    for {
      uBRN <- variables.fields.get(uBRN)
      optUBRNref = variables.fields.get(UBRNref)
      enterpriseLink <- toEnterpriseLink(variables)
    } yield LegalUnit(UBRN(uBRN), optUBRNref, enterpriseLink)

  private def toEnterpriseLink(variables: Row): Option[EnterpriseLink] =
    for {
      ern <- variables.fields.get(ern)
      optEntref = variables.fields.get(entref)
    } yield EnterpriseLink(Ern(ern), optEntref)
}
