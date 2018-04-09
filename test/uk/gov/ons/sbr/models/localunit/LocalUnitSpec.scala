package uk.gov.ons.sbr.models.localunit

import org.scalatest.{ FreeSpec, Matchers }
import play.api.libs.json.Json
import support.sample.SampleLocalUnit

class LocalUnitSpec extends FreeSpec with Matchers {

  private trait Fixture extends SampleLocalUnit {
    private def string(name: String, value: String): Option[String] =
      Some(s""""$name":"$value"""")

    private def optionalString(name: String, optValue: Option[String]): Option[String] =
      optValue.flatMap(string(name, _))

    private def withValues(values: Option[String]*): String =
      values.flatten.mkString(",")

    def expectedJsonStrOf(localUnit: LocalUnit): String =
      s"""
         |{
         | "lurn":"${localUnit.lurn.value}",
         | "luref":"${localUnit.luref}",
         | "name":"${localUnit.name}",
         | "tradingStyle":"${localUnit.tradingStyle}",
         | "sic07":"${localUnit.sic07}",
         | "employees":${localUnit.employees},
         | "enterprise": {${withValues(
             string("ern", localUnit.enterprise.ern.value),
             optionalString("entref", localUnit.enterprise.entref))}
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
    "can be represented as JSON" - {
      "when all fields are defined" in new Fixture {
        Json.toJson(SampleAllValuesLocalUnit) shouldBe Json.parse(expectedJsonStrOf(SampleAllValuesLocalUnit))
      }

      "when only the mandatory fields are defined" in new Fixture {
        Json.toJson(SampleMandatoryValuesLocalUnit) shouldBe Json.parse(expectedJsonStrOf(SampleMandatoryValuesLocalUnit))
      }
    }
  }
}
