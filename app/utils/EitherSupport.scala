package utils

object EitherSupport {
  def sequence[A, B](eithers: Seq[Either[A, B]]): Either[A, Seq[B]] = {
    val zeroAcc: Either[A, Seq[B]] = Right(Seq.empty[B])
    eithers.foldRight(zeroAcc) { (e, acc) =>
      acc.right.flatMap { bs =>
        e.fold(a => Left(a), b => Right(b +: bs))
      }
    }
  }
}
