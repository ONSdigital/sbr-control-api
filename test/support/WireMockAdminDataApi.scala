package support

import com.github.tomakehurst.wiremock.client.WireMock.{ head, urlEqualTo }
import com.github.tomakehurst.wiremock.client.{ MappingBuilder, WireMock }
import org.scalatest.fixture
import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.unitlinks.UnitId

trait WireMockAdminDataApi extends WithWireMock with ApiResponse { this: fixture.Suite =>
  def aVatRefLookupRequest(withVatUnitId: UnitId, withPeriod: Period): MappingBuilder =
    head(urlEqualTo(s"/v1/records/${withVatUnitId.value}/periods/${Period.asString(withPeriod)}"))

  val stubAdminDataApiFor: MappingBuilder => Unit =
    WireMock.stubFor
}
