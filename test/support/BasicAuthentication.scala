package support

trait BasicAuthentication {
  case class Authorization(username: String, password: String)

  object Authorization {
    val Name = "Authorization"

    private def authValue(auth: Authorization) =
      s"${auth.username}:${auth.password}"

    def value(auth: Authorization): String =
      s"Basic ${Base64.encode(authValue(auth))}"
  }
}
