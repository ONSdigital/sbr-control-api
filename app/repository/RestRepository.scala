package repository

import repository.RestRepository.Row

import scala.concurrent.Future

trait RestRepository {
  def get(table: String, rowKey: String, columnGroup: String): Future[Seq[Row]]
}

object RestRepository {
  type Row = Map[String, String]
}