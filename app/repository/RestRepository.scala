package repository

import scala.concurrent.Future

import repository.RestRepository.{ ErrorMessage, Row }

trait RestRepository {
  def findRow(table: String, rowKey: String, columnFamily: String): Future[Either[ErrorMessage, Option[Row]]]
  def findRows(table: String, query: String, columnFamily: String): Future[Either[ErrorMessage, Seq[Row]]]
}

object RestRepository {
  type ErrorMessage = String
  type RowKey = String

  case class Row(rowKey: RowKey, fields: Map[String, String])
}