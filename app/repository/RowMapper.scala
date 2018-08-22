package repository

import repository.RestRepository.Row

trait RowMapper[A] {
  def fromRow(variables: Row): Option[A]
}
