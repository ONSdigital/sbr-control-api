package repository.solr

import java.io.IOException

import com.typesafe.scalalogging.LazyLogging
import javax.inject.Inject
import org.apache.solr.client.solrj.impl.CloudSolrClient
import org.apache.solr.client.solrj.response.QueryResponse
import org.apache.solr.client.solrj.{ SolrQuery, SolrServerException }
import org.apache.solr.common.SolrDocument
import repository.LegalUnitRepository
import repository.RestRepository.ErrorMessage
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.legalunit.{ Crn, LegalUnit, Ubrn, Uprn }
import uk.gov.ons.sbr.models.{ Address, Period }

import scala.collection.mutable
import scala.concurrent.{ ExecutionContext, Future }

class SolrLegalUnitRepository @Inject() (client: CloudSolrClient)(implicit solrExecutionContext: ExecutionContext) extends LegalUnitRepository with LazyLogging {

  def solrRetrieveLegalUnit(ubrn: Ubrn): Future[Either[ErrorMessage, Option[LegalUnit]]] =
    Future(doRetrieveLegalUnit(ubrn))

  /*
   * NOTE: an alternative approach would be to simply let any exceptions be raised, which will fail the Future.
   * This could then be handled by Future recovery, generating a Future.successful(Left(ErrorMessage))
   */
  private def doRetrieveLegalUnit(ubrn: Ubrn): Either[ErrorMessage, Option[LegalUnit]] = {
    //    val client = clientFactory()
    try {
      val queryResponse = client.query("leu", new SolrQuery(s"ubrn:$ubrn"))
      logger.info(s"Received response [$queryResponse]")

      fromQueryResponse(queryResponse) match {
        case Nil => Right(None)
        case single :: Nil => Right(Some(single))
        case _ => Left("Found too many rows for lookup by id")
      }
    } catch {
      case ioe: IOException => handleRetrieveError(ioe)
      case sse: SolrServerException => handleRetrieveError(sse)
    }
    //    } finally {
    //      try {
    //        client.close()
    //      } catch {
    //        case ioe: IOException =>
    //          logger.warn(s"Failed to close SolrClient [${ioe.getMessage}].  Ignoring ...")
    //      }
    //    }
  }

  private def handleRetrieveError(cause: Exception): Either[ErrorMessage, Nothing] = {
    logger.error(s"Solr lookup failed [${cause.getMessage}]", cause)
    Left(cause.getMessage)
  }

  //  /*
  //   * Initial implementation using blocking I/O just to proven Solr connectivity.
  //   */
  //  def synchronousSolrRetrieveLegalUnit(ubrn: Ubrn): Future[Either[ErrorMessage, Option[LegalUnit]]] = {
  //    val client = solrClientBuilder.build()
  //    try {
  //      val queryResponse = client.query("leu", new SolrQuery(s"ubrn:$ubrn"))
  //      logger.info(s"Received response [$queryResponse]")
  //
  //      fromQueryResponse(queryResponse) match {
  //        case Nil => Future.successful(Right(None))
  //        case single :: Nil => Future.successful(Right(Some(single)))
  //        case _ => Future.successful(Left("Found too many rows for lookup by id"))
  //      }
  //    } catch {
  //      case ioe: IOException => handleRetrievalError(ioe)
  //      case sse: SolrServerException => handleRetrievalError(sse)
  //    } finally {
  //      try {
  //        client.close()
  //      } catch {
  //        case ioe: IOException =>
  //          logger.warn(s"Failed to close SolrClient [${ioe.getMessage}].  Ignoring ...")
  //      }
  //    }
  //  }

  //  private def handleRetrievalError(cause: Exception): Future[Either[ErrorMessage, Option[LegalUnit]]] = {
  //    logger.error(s"Solr lookup failed [${cause.getMessage}]", cause)
  //    Future.successful(Left(cause.getMessage))
  //  }

  // can use same code as solrs here - as it uses the solrj types in its query api
  // NOTE: for efficiency we could check the result count value rather than the length of the result list
  private def fromQueryResponse(queryResponse: QueryResponse): List[LegalUnit] = {
    val buffer = new mutable.ListBuffer[LegalUnit]

    val it: java.util.Iterator[SolrDocument] = queryResponse.getResults.iterator()
    while (it.hasNext) {
      val doc = it.next()

      val ubrn = Ubrn(doc.getFieldValue("ubrn").toString)
      val name = fromMultivalued[String](doc, "nameline").getOrElse("")
      val legalStatus = doc.getFieldValue("legalstatus").toString
      val tradingStatus = fromMultivalued[String](doc, "trading_status")
      val tradingStyle = fromMultivalued[String](doc, "tradstyle")
      val sic07 = fromMultivalued[Long](doc, "sic07").map(_.toString).getOrElse("")
      val turnover = fromMultivalued[Long](doc, "turnover").map(_.toInt)
      val jobs = fromMultivalued[Long](doc, "paye_jobs").map(_.toInt)
      val line1 = fromMultivalued[String](doc, "address1").getOrElse("")
      val line2 = fromMultivalued[String](doc, "address2")
      val line3 = fromMultivalued[String](doc, "address3")
      val line4 = fromMultivalued[String](doc, "address4")
      val line5 = fromMultivalued[String](doc, "address5")
      val postcode = fromMultivalued[String](doc, "postcode").getOrElse("")
      val address = Address(line1, line2, line3, line4, line5, postcode)
      val birthDate = fromMultivalued[String](doc, "birth_date").getOrElse("")
      val deathDate = fromMultivalued[String](doc, "death_date")
      val deathCode = fromMultivalued[String](doc, "death_code")
      val crn = if (doc.containsKey("crn")) Some(Crn(doc.getFieldValue("crn").toString)) else None
      val uprn = if (doc.containsKey("uprn")) Some(Uprn(doc.getFieldValue("uprn").toString)) else None

      val leu = LegalUnit(ubrn, name, legalStatus, tradingStatus, tradingStyle, sic07, turnover, jobs, address, birthDate, deathDate, deathCode, crn, uprn)
      buffer.append(leu)
    }

    buffer.toList
  }

  private def fromMultivalued[T](doc: SolrDocument, name: String): Option[T] =
    if (doc.containsKey(name))
      doc.getFieldValue(name) match {
        case v: java.util.List[T] if v.isEmpty => None
        case v: java.util.List[T] => Some(v.get(0))
      }
    else None

  override def retrieveLegalUnit(ern: Ern, period: Period, ubrn: Ubrn): Future[Either[ErrorMessage, Option[LegalUnit]]] = ???

  override def findLegalUnitsForEnterprise(ern: Ern, period: Period): Future[Either[ErrorMessage, Seq[LegalUnit]]] = ???
}
