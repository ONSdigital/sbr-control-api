package services

import uk.gov.ons.sbr.models.units.{EnterpriseUnit, UnitLinks}

/**
 * Created by coolit on 01/02/2018.
 */
trait DataAccess {

  def getUnitLinks(id: String, period: String): Option[List[UnitLinks]]

  def getEnterprise(id: String, period: String): Option[EnterpriseUnit]

  def getStatUnitLinks(id: String, category: String, period: String): Option[UnitLinks]

//  @throws(classOf[DateTimeParseException])
//  def validateYearMonth(key: String, raw: String) = {
//    val yearAndMonth = Try(YearMonth.parse(raw, DateTimeFormatter.ofPattern("yyyyMM")))
//    yearAndMonth match {
//      case Success(s) =>
//        ReferencePeriod(key, s)
//      case Failure(ex: DateTimeParseException) =>
//        logger.error("cannot parse date to YearMonth object", ex)
//        InvalidReferencePeriod(key, ex)
//    }
//  }

//  def tryAsResponse(parseToJson: Try[JsValue], errMsg: Option[String] = Some("Could not parse to json")): Result = parseToJson match {
//    case Success(s) => Ok(s)
//    case Failure(ex) =>
//      logger.error("Failed to parse instance to expected json format", ex)
//      BadRequest(errAsJson(BAD_REQUEST, "bad_request",
//        s"${errMsg.getOrElse("Could not perform action")} with exception $ex"))
//  }

//  def matchByParams(id: Option[String], date: Option[String] = None)(implicit request: Request[AnyContent]): RequestEvaluation = {
//    val key = id.orElse(request.getQueryString("id")).getOrElse("")
//    if (key.length >= minKeyLength) {
//      date match {
//        case None => IdRequest(key)
//        case Some(s) => validateYearMonth(key, s)
//      }
//    } else {
//      logger.debug(s"Given key [$key] as argument is invalid")
//      InvalidKey(key)
//    }
//  }

//  def toOption[X](o: Optional[X]) = if (o.isPresent) Some(o.get) else None
//
//  def toJavaOptional[A](o: Option[A]): Optional[A] =
//    o match { case Some(a) => Optional.ofNullable(a); case _ => Optional.empty[A] }
//
//  /**
//   * @param v - value param to convert
//   * @param msg - overriding msg option
//   * @tparam Z - java data type for value param
//   * @return Future[Result]
//   */
//  def resultMatcher[Z](v: Optional[Z], msg: Option[String] = None): Future[Result] = {
//    Future { toOption[Z](v) }.map {
//      case Some(x: java.util.List[UnitLinks]) => {
//        tryAsResponse(Try(Json.toJson(x.toList)))
//      }
//      case Some(x: UnitLinks) => {
//        tryAsResponse(Try(Json.toJson(x)))
//      }
//      case Some(x: ChildUnit) => {
//        tryAsResponse(Try(Json.toJson(x)))
//      }
//      case Some(x: java.util.List[ChildUnit]) => {
//        tryAsResponse(Try(Json.toJson(x.toList)))
//      }
//      case Some(x: java.util.List[StatisticalUnit]) =>
//        tryAsResponse(Try(Json.toJson(x.toList.map { v => UnitLinks(v) })))
//      case Some(x: Enterprise) =>
//        tryAsResponse(Try(Json.toJson(EnterpriseUnit(x))))
//      case Some(x: EnterpriseUnit) =>
//        tryAsResponse(Try(Json.toJson(x)))
//      case Some(x: StatisticalUnitLinks) =>
//        tryAsResponse(Try(Json.toJson(KnownUnitLinks(x))))
//      case ex =>
//        logger.debug("Invalid response from data source", s"$ex")
//        BadRequest(errAsJson(BAD_REQUEST, "bad_request", msg.getOrElse(s"Could not parse returned response [ex: $ex]")))
//    }
//  }

//  def responseException: PartialFunction[Throwable, Result] = {
//    case ex: DateTimeParseException =>
//      logger.error("cannot parse date to to specified date format", ex)
//      BadRequest(errAsJson(BAD_REQUEST, "invalid_date", s"cannot parse date exception found $ex"))
//    case ex: RuntimeException =>
//      logger.error(s"RuntimeException ${ex.getMessage}", ex.getCause)
//      InternalServerError(errAsJson(INTERNAL_SERVER_ERROR, "runtime_exception", ex.getMessage))
//    case ex: ServiceUnavailableException =>
//      logger.error(s"ServiceUnavailableException ${ex.getMessage}", ex.getCause)
//      ServiceUnavailable(errAsJson(SERVICE_UNAVAILABLE, "service_unavailable", s"$ex"))
//    case ex: TimeoutException =>
//      logger.error(s"TimeoutException ${ex.getMessage}", ex.getCause)
//      RequestTimeout(errAsJson(REQUEST_TIMEOUT, "request_timeout", s"This may be due to connection being blocked. $ex"))
//    case ex =>
//      logger.error(s"Unknown error has occurred with exception $ex", ex.getCause)
//      InternalServerError(errAsJson(INTERNAL_SERVER_ERROR, "internal_server_error", s"$ex."))
//  }

//  protected def invalidSearchResponses(invalidRequest: RequestEvaluation) = {
//    invalidRequest match {
//      case (y: InvalidReferencePeriod) => BadRequest(errAsJson(BAD_REQUEST, "invalid_date",
//        s"cannot parse date with exception ${y.exception}")).future
//      case (i: InvalidKey) =>
//        BadRequest(errAsJson(BAD_REQUEST, "invalid_key",
//          s"invalid id ${i.id}. Check key size[$minKeyLength].")).future
//      case _ =>
//        BadRequest(errAsJson(BAD_REQUEST, "missing_param", s"No query specified.")).future
//    }
//  }

//  protected def parseYearMonth(ym: YearMonth) = ym.toString.replace("-", "").toLong
}

