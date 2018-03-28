package uk.gov.ons.sbr.models.localunit

import org.scalatest.{ FreeSpec, Matchers }
import play.api.libs.json.Json
import support.sample.SampleLocalUnit

class LocalUnitSpec extends FreeSpec with Matchers {

  private trait Fixture extends SampleLocalUnit {
    def expectedJsonStrOf(localUnit: LocalUnit): String =
      s"""
         |{
         | "lurn":"${localUnit.lurn.value}",
         | "luref":"${localUnit.luref}",
         | "name":"${localUnit.name}",
         | "tradingStyle":"${localUnit.tradingStyle}",
         | "sic07":"${localUnit.sic07}",
         | "employees":${localUnit.employees},
         | "enterprise": {
         |   "ern":"${localUnit.enterprise.ern.value}",
         |   "entref":"${localUnit.enterprise.entref}"
         | },
         | "address": {
         |   "line1":"${localUnit.address.line1}",
         |   "line2":"${localUnit.address.line2}",
         |   "line3":"${localUnit.address.line3}",
         |   "line4":"${localUnit.address.line4}",
         |   "line5":"${localUnit.address.line5}",
         |   "postcode":"${localUnit.address.postcode}"
         | }
         |}
       """.stripMargin
  }

  "A LocalUnit" - {
    "can be represented as JSON" in new Fixture {
      Json.toJson(SampleLocalUnit) shouldBe Json.parse(expectedJsonStrOf(SampleLocalUnit))
    }
  }
}
