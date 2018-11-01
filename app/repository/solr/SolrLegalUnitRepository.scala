package repository.solr

import java.util

import com.typesafe.scalalogging.LazyLogging
import io.ino.solrs.AsyncSolrClient
import javax.inject.Inject
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.response.QueryResponse
import org.apache.solr.common.SolrDocument
import repository.LegalUnitRepository
import repository.RestRepository.ErrorMessage
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.legalunit.{ Crn, LegalUnit, Ubrn, Uprn }
import uk.gov.ons.sbr.models.{ Address, Period }

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SolrLegalUnitRepository @Inject() (solrClient: AsyncSolrClient[Future]) extends LegalUnitRepository with LazyLogging {

  def find(ubrn: Ubrn): Future[Either[ErrorMessage, Option[LegalUnit]]] = {
    //val solrClient: AsyncSolrClient[Future] = AsyncSolrClient.Builder(loadBalancer).withRetryPolicy(RetryPolicy.TryAvailableServers).build

    val futQueryResponse: Future[QueryResponse] = solrClient.query(collection = "leu", new SolrQuery(s"ubrn:$ubrn"))

    val result = futQueryResponse.map { qr =>
      logger.info(s"Received response [$qr]")

      // as the library wraps solrj - the api to inspect results is imperative in nature ...
      // Note need to deal with:
      // - missing mandatory values
      // - incorrect types
      // - multi-valued fields (defined schema may avoid this?)
      val buffer = new mutable.ListBuffer[LegalUnit]

      val it: util.Iterator[SolrDocument] = qr.getResults.iterator()
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

      buffer.toList match {
        case Nil => Right(None)
        case x :: xs if xs.isEmpty => Right(Some(x))
        case _ => Left("Found too many rows for lookup by id")
      }
    }.recover {
      case t: Throwable => Left(t.getMessage)
    }

    //    result.onComplete { _ =>
    //      solrClient.shutdown()
    //      logger.info("Shutdown solrClient")
    //    }
    result
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
