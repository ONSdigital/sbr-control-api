package support.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.{ MappingBuilder, WireMock }

class WireMockSupport private (val wireMockServer: WireMockServer, val wireMock: WireMock)

object WireMockSupport {
  def start(port: Int): WireMockSupport = {
    val wireMock = new WireMock(port)
    val server = new WireMockServer(port)
    server.start()

    new WireMockSupport(server, wireMock)
  }

  def stop(wireMockSupport: WireMockSupport): Unit =
    try wireMockSupport.wireMock.shutdown()
    finally wireMockSupport.wireMockServer.stop()

  def registerMapping(wireMockSupport: WireMockSupport)(mappingBuilder: MappingBuilder): WireMockSupport = {
    wireMockSupport.wireMock.register(mappingBuilder)
    wireMockSupport
  }

  def port(wireMockSupport: WireMockSupport): Int =
    wireMockSupport.wireMockServer.port()
}
