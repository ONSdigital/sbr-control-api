package controllers.v1

import io.swagger.annotations._
import javax.inject.{ Inject, Singleton }
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json.toJson
import play.api.mvc.{ Action, Controller, Result }
import repository.LocalUnitRepository
import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.localunit.{ LocalUnit, Lurn }

@Api("Search")
@Singleton
class LocalUnitController @Inject() (repository: LocalUnitRepository) extends Controller {
  @ApiOperation(
    value = "Json representation of the Local Unit with specified ERN, LURN and Period",
    notes = "Requires an exact match of ERN, LURN and Period",
    response = classOf[LocalUnit],
    responseContainer = "object",
    code = 200,
    httpMethod = "GET"
  )
  def retrieveLocalUnit(
    @ApiParam(value = "Enterprise Reference Number (ERN)", example = "1000000012", required = true) ernStr: String,
    @ApiParam(value = "Period (unit load date)", example = "201803", required = true) periodStr: String,
    @ApiParam(value = "Local Unit Reference Number (LURN)", example = "900000011", required = true) lurnStr: String
  ) = Action.async {
    repository.retrieveLocalUnit(Ern(ernStr), Period.fromString(periodStr), Lurn(lurnStr)).map { optLocalUnit =>
      optLocalUnit.fold[Result](NotFound) { localUnit =>
        Ok(toJson(localUnit))
      }
    }
  }
}
