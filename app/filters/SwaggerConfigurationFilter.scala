package filters

import java.util

import io.swagger.core.filter.SwaggerSpecFilter
import io.swagger.model.ApiDescription
import io.swagger.models.parameters.Parameter
import io.swagger.models.properties.Property
import io.swagger.models.{ Model, Operation }

/*
 * There is no equivalent of Java's @SuppressWarnings in Scala.  However, in Scala 2.11 we can stop deprecation
 * warnings for ApiDescription by deprecating the class that is using it ...
 * This is discussed here: https://issues.scala-lang.org/browse/SI-7934
 *
 * As for the Swagger issue itself, we are using the latest version of swagger-play2 that is compatible with Play 2.5,
 * and it is pulling in a version of swagger-core that defines SwaggerSpecFilter using the deprecated ApiDescription.
 * We will hopefully be able to address this as part of a Play 2.6 upgrade.
 * Details of SwaggerDefinition can be found at: https://github.com/swagger-api/swagger-core/wiki/Annotations-1.5.X#swaggerdefinition
 */
@deprecated(message = "Swagger ApiDescription is deprecated - use SwaggerDefinition instead", since = "Swagger 1.5")
class SwaggerConfigurationFilter extends SwaggerSpecFilter {

  private val parametersNotAllowed: List[String] = List()
  private val propertiesNotAllowed: List[String] = List()

  def isParamAllowed(
    parameter: Parameter,
    operation: Operation,
    api: ApiDescription,
    params: util.Map[String, util.List[String]],
    cookies: util.Map[String, String],
    headers: util.Map[String, util.List[String]]
  ): Boolean = filter(parametersNotAllowed, parameter.getName)

  def isPropertyAllowed(
    model: Model,
    property: Property,
    propertyName: String,
    params: util.Map[String, util.List[String]],
    cookies: util.Map[String, String],
    headers: util.Map[String, util.List[String]]
  ): Boolean = filter(propertiesNotAllowed, property.getName)

  def isOperationAllowed(
    operation: Operation,
    api: ApiDescription,
    params: util.Map[String, util.List[String]],
    cookies: util.Map[String, String],
    headers: util.Map[String, util.List[String]]
  ): Boolean = true

  def filter(terms: List[String], f: => String): Boolean =
    !terms.contains(f)
}
