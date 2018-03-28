package uk.gov.ons.sbr.models.enterprise

import org.scalatest.{ FreeSpec, Matchers }
import play.api.libs.json.Json

class EnterpriseLinkSpec extends FreeSpec with Matchers {

  private trait Fixture {
    def expectedJsonStrOf(enterpriseLink: EnterpriseLink): String =
      s"""{"ern":"${enterpriseLink.ern.value}", "entref":"${enterpriseLink.entref}"}"""
  }

  "An EnterpriseLink" - {
    "can be represented as JSON" in new Fixture {
      val anEnterpriseLink = EnterpriseLink(Ern("1000000012"), entref = "999999999")

      Json.toJson(anEnterpriseLink) shouldBe Json.parse(expectedJsonStrOf(anEnterpriseLink))
    }
  }
}
