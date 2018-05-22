package it.fixture

import play.api.libs.json._

import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.unitlinks.{ UnitId, UnitLinks, UnitType }

object ReadsUnitLinks {
  implicit val unitLinksReads: Reads[UnitLinks] = TestUnitLinksReads

  object TestUnitLinksReads extends Reads[UnitLinks] {

    case class TestExternalForm(id: UnitId, period: Period, parents: Option[Map[String, UnitId]], children: Option[Map[String, UnitType]], unitType: UnitType)

    implicit val periodReads: Reads[Period] = Reads.StringReads.map(Period.fromString)
    implicit val unitTypeReads: Reads[UnitType] = Reads.StringReads.map(UnitType.fromAcronym)
    implicit val unitIdReads: Reads[UnitId] = Reads.StringReads.map(UnitId(_))

    implicit val readsTestExternalForm: Reads[TestExternalForm] = Json.reads[TestExternalForm]

    override def reads(json: JsValue): JsResult[UnitLinks] =
      json.validate[TestExternalForm].map(fromTestExternalForm)

    private def fromTestExternalForm(testExForm: TestExternalForm): UnitLinks =
      UnitLinks(testExForm.id, testExForm.period, testExForm.parents.map(fromTestExternalParents),
        testExForm.children.map(fromTestExternalChildren), testExForm.unitType)

    private def fromTestExternalParents(testExFormParent: Map[String, UnitId]): Map[UnitType, UnitId] =
      testExFormParent.map {
        case (k, v) =>
          UnitType.fromAcronym(k) -> v
      }

    private def fromTestExternalChildren(testExFormChild: Map[String, UnitType]): Map[UnitId, UnitType] =
      testExFormChild.map {
        case (k, v) =>
          UnitId(k) -> v
      }

  }
}
