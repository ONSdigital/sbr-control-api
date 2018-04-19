package repository.hbase.enterprise

import scala.util.{ Failure, Success, Try }

import uk.gov.ons.sbr.models.enterprise.{ Enterprise, Ern }

import repository.RestRepository.Row
import repository.RowMapper
import repository.hbase.enterprise.EnterpriseUnitColumns._

object EnterpriseUnitRowMapper extends RowMapper[Enterprise] {

  override def fromRow(variables: Row): Option[Enterprise] =
    for {
      ern <- variables.get(ern)
      entref <- variables.get(entref)
      name <- variables.get(name)
      postcode <- variables.get(postcode)
      legalStatus <- variables.get(legalStatus)

      employeesStr = variables.get(employees)
      employeeOptTry = asInt(employeesStr)
      if invalidInt(employeeOptTry)
      employeeOptInt = parseTry(employeeOptTry)

      jobsStr = variables.get(jobs)
      jobsOptTry = asInt(jobsStr)
      if invalidInt(jobsOptTry)
      jobsOptInt = parseTry(jobsOptTry)

    } yield Enterprise(Ern(ern), entref, name, postcode, legalStatus, employeeOptInt, jobsOptInt)

  private def parseTry(valueOptTry: Option[Try[Int]]) =
    valueOptTry.fold[Option[Int]](None) {
      case Success(n) => Some(n)
      case Failure(_) => throw new AssertionError()
    }

  private def asInt(fieldAsStr: Option[String]) = fieldAsStr.map(x => Try(x.toInt))

  private def invalidInt(fieldOptTry: Option[Try[Int]]) = fieldOptTry.fold(true)(_.isSuccess)

}
