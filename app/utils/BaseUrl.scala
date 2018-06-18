package utils

case class BaseUrl(protocol: String, host: String, port: Int, prefix: Option[String])

object BaseUrl {
  object Protocol {
    val Http = "http"
  }

  def asUrlString(baseUrl: BaseUrl): String =
    s"${baseUrl.protocol}://${baseUrl.host}:${baseUrl.port}/${baseUrl.prefix.fold("")(p => if (p.trim.isEmpty()) "" else p + "/")}"
}