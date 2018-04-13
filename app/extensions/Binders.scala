package extensions

import play.api.mvc._

/**
 * This is for using Options in the routes file in Play version 2.1 and above
 * https://stackoverflow.com/questions/14980952/routes-with-optional-parameter-play-2-1-scala
 */
object Binders {
  implicit def OptionBindable[T: PathBindable] = new PathBindable[Option[T]] {
    def bind(key: String, value: String): Either[String, Option[T]] =
      implicitly[PathBindable[T]].
        bind(key, value).
        fold(
          left => Left(left),
          right => Right(Some(right))
        )

    def unbind(key: String, value: Option[T]): String = value map (_.toString) getOrElse ""
  }
}
