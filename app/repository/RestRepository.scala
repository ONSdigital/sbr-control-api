package repository

import scala.concurrent.Future
import repository.RestRepository.{ ErrorMessage, Field, Row, RowKey }

trait RestRepository {
  def findRow(table: String, rowKey: RowKey, columnFamily: String): Future[Either[ErrorMessage, Option[Row]]]
  def findRows(table: String, query: String, columnFamily: String): Future[Either[ErrorMessage, Seq[Row]]]

  def update(table: String, rowKey: RowKey, checkField: Field, updateField: Field): Future[UpdateResult]
}

sealed trait UpdateResult
object UpdateRejected extends UpdateResult
object UpdateApplied extends UpdateResult
object UpdateConflicted extends UpdateResult
object UpdateTargetNotFound extends UpdateResult
object UpdateFailed extends UpdateResult

object RestRepository {
  type ErrorMessage = String
  type RowKey = String
  type Field = (String, String)

  case class Row(rowKey: RowKey, fields: Map[String, String])
}