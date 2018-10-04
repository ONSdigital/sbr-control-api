package support.wiremock

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import play.api.http.Status._

trait ApiResponse {
  def anOkResponse(): ResponseDefinitionBuilder =
    aResponse().withStatus(OK)

  def aNoContentResponse(): ResponseDefinitionBuilder =
    aResponse().withStatus(NO_CONTENT)

  def aNotFoundResponse(): ResponseDefinitionBuilder =
    aResponse().withStatus(NOT_FOUND)

  def aNotModifiedResponse(): ResponseDefinitionBuilder =
    aResponse().withStatus(NOT_MODIFIED)

  def anInternalServerError(): ResponseDefinitionBuilder =
    aResponse().withStatus(INTERNAL_SERVER_ERROR)

  def aServiceUnavailableResponse(): ResponseDefinitionBuilder =
    aResponse().withStatus(SERVICE_UNAVAILABLE)
}
