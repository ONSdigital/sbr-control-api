package uk.gov.ons.sbr.models.enterprise

import play.api.libs.json.Json
import org.scalatest.{ FreeSpec, Matchers, OptionValues }

import support.sample.SampleEnterpriseUnit
import support.JsonString._

class EnterpriseSpec extends FreeSpec with Matchers with OptionValues {

  private trait Fixture extends SampleEnterpriseUnit {

    def expectedJsonOutput(ent: Enterprise): String =
      s"""
         |{${
        withValues(
          string(name = "ern", value = ent.ern.value),
          string(name = "entref", value = ent.entref),
          string(name = "name", value = ent.name),
          string(name = "postcode", value = ent.postcode),
          string(name = "legalStatus", value = ent.legalStatus),
          optionalInt(name = "employees", optValue = ent.employees),
          optionalInt(name = "jobs", optValue = ent.jobs)
        )
      }
         |}""".stripMargin
  }

  "A enterprise" - {
    "should have a JSON representation" - {
      "when all field are included - both mandatory and optional" in new Fixture {
        Json.toJson(SampleEnterpriseWithAllFields) shouldBe Json.parse(expectedJsonOutput(SampleEnterpriseWithAllFields))
      }

      "when only mandatory field are given - i.e. excluding optional" in new Fixture {
        Json.toJson(SampleEnterpriseWithNoOptionalFields) shouldBe Json.parse(expectedJsonOutput(SampleEnterpriseWithNoOptionalFields))
      }
    }
  }

}

