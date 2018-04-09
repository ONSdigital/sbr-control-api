package uk.gov.ons.sbr.models.enterprise

import org.scalatest.{ FreeSpec, Matchers }
import play.api.libs.json.Json

class EnterpriseLinkSpec extends FreeSpec with Matchers {

  private trait Fixture {
    private def stringField(name: String, value: String): String =
      s""""$name":"$value""""

    def expectedJsonStrOf(enterpriseLink: EnterpriseLink): String =
      Seq(
        Some(stringField("ern", enterpriseLink.ern.value)),
        enterpriseLink.entref.map(stringField("entref", _))
      ).
        flatten.mkString("{", ",", "}")
  }

  "An EnterpriseLink" - {
    "can be represented as JSON" - {
      "when all fields are defined" in new Fixture {
        val anEnterpriseLink = EnterpriseLink(Ern("1000000012"), entref = Some("999999999"))

        Json.toJson(anEnterpriseLink) shouldBe Json.parse(expectedJsonStrOf(anEnterpriseLink))
      }

      "when only mandatory fields are defined" in new Fixture {
        val anEnterpriseLink = EnterpriseLink(Ern("1000000012"), entref = None)

        Json.toJson(anEnterpriseLink) shouldBe Json.parse(expectedJsonStrOf(anEnterpriseLink))
      }
    }
  }
}
