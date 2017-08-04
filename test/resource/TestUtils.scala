package resource

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.{ JsDefined, JsLookupResult }
import play.api.test.FakeRequest
import play.api.test.Helpers._

/**
 * Created by Ameen on 15/07/2017.
 */

trait TestUtils extends PlaySpec with GuiceOneAppPerSuite {

  protected[this] def fakeRequest(url: String, method: String = GET) =
    route(app, FakeRequest(method, url)).getOrElse(sys.error(s"Route $url does not exist"))

  def getValue(json: Option[String]): String = json match {
    case Some(x: String) => s"${x}"
    case _ => sys.error("No Value failed. Forcing test failure")
  }

  def getJsValue(elem: JsLookupResult) = elem match {
    case JsDefined(y) => s"${y}"
    case _ => sys.error("No JsValue found. Forcing test failure")
  }

}
