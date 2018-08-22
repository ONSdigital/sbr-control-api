package support

import java.nio.charset.StandardCharsets.UTF_8

object Base64 {
  def encode(value: String): String =
    java.util.Base64.getEncoder.encodeToString(value.getBytes(UTF_8.name()))
}
