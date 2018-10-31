package repository.solr

import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.impl.CloudSolrClient
import org.apache.solr.client.solrj.response.QueryResponse
import repository.LegalUnitRepository
import repository.RestRepository.ErrorMessage
import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.legalunit.{LegalUnit, Ubrn}

import scala.collection.JavaConverters._
import scala.concurrent.Future

class SolrLegalUnitRepository extends LegalUnitRepository {
  def solrRetrieveLegalUnit(ubrn: Ubrn): Future[Either[ErrorMessage, Option[LegalUnit]]] = {
    val zkHosts = List("localhost:9983")
    val clientBuilder = new CloudSolrClient.Builder(zkHosts.asJava)

    val client = clientBuilder.build()
    try {
      val response = client.query("leu", new SolrQuery(s"ubrn:$ubrn"))
    } finally {
      client.close()
    }
  }

  override def retrieveLegalUnit(ern: Ern, period: Period, ubrn: Ubrn): Future[Either[ErrorMessage, Option[LegalUnit]]] = ???

  override def findLegalUnitsForEnterprise(ern: Ern, period: Period): Future[Either[ErrorMessage, Seq[LegalUnit]]] = ???
}
