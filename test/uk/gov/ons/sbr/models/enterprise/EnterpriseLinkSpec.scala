package uk.gov.ons.sbr.models.enterprise

import org.scalatest.{ FreeSpec, Matchers }
import play.api.libs.json.Json
import support.JsonString
import support.JsonString.{ optionalString, string }

class EnterpriseLinkSpec extends FreeSpec with Matchers {

  private trait Fixture {
    def expectedJsonStrOf(enterpriseLink: EnterpriseLink): String =
      JsonString.withObject(
        string("ern", enterpriseLink.ern.value),
        optionalString("entref", enterpriseLink.entref)
      )
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
