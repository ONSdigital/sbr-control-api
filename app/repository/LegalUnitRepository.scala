package repository

import repository.RestRepository.ErrorMessage
import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.legalunit.{ LegalUnit, UBRN }

import scala.concurrent.Future

trait LegalUnitRepository {
  def retrieveLegalUnit(ern: Ern, period: Period, ubrn: UBRN): Future[Either[ErrorMessage, Option[LegalUnit]]]
  def findLegalUnitsForEnterprise(ern: Ern, period: Period): Future[Either[ErrorMessage, Seq[LegalUnit]]]
}
