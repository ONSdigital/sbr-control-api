package support

object JsonString {
  def string(name: String, value: String): Option[String] =
    Some(s""""$name":"$value"""")

  def optionalString(name: String, optValue: Option[String]): Option[String] =
    optValue.flatMap(string(name, _))

  def withValues(values: Option[String]*): String =
    values.flatten.mkString(",")

  def withObject(values: Option[String]*): String =
    values.flatten.mkString("{", ",", "}")
}
