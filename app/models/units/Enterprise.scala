package models.units

import com.google.inject.ImplementedBy
import play.api.libs.json.{ Json, OFormat, JsValue }
import uk.gov.ons.sbr.data.domain.Enterprise

import scala.collection.JavaConversions._
/**
 * Created by haqa on 08/08/2017.
 */
//@ImplementedBy()
case class EnterpriseKey(
  id: String,
  values: Map[String, String]
)

object EnterpriseKey {

  implicit val unitFormat: OFormat[EnterpriseKey] = Json.format[EnterpriseKey]

  def addKey(o: Enterprise): Map[String, String] = o.getVariables.toMap + ("key" -> o.getKey)

  def toJson(o: Enterprise): JsValue = Json.toJson(addKey(o))

}
