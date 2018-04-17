package repository

import repository.RestRepository.{ ErrorMessage, Row }

import scala.concurrent.Future

trait RestRepository {
  def findRow(table: String, rowKey: String, columnGroup: String): Future[Either[ErrorMessage, Option[Row]]]
  def findRows(table: String, query: String, columnGroup: String): Future[Either[ErrorMessage, Seq[Row]]]
}

object RestRepository {
  type ErrorMessage = String
  type Row = Map[String, String]
}