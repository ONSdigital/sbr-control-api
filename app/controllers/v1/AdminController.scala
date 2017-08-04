package controllers.v1

import java.time.YearMonth

import io.swagger.annotations.Api
//import java.util

import uk.gov.ons.sbr.data.domain.UnitType

/**
  * Created by haqa on 04/08/2017.
  */
@Api("Modify")
class AdminController extends ControllerUtils {

  def updateLinks (date: String, id: String, units: String, parents: String, children: String ) = ???
//  {
//  // param: date: YearMonth, id: String, units: ??? , parents: Map[???, String], children: String
//    val yearAndMonth : YearMonth = new YearMonth(date)
//    requestLinks.updateUnitLinks(yearAndMonth, id, ???, ???, children)
//
//  }


  def updateEnterprise (period: String, id: String, fieldName: String, newValue: String) = ???
  // (YearMonth referencePeriod, String enterpriseReferenceNumber, String variableName, String newValue)


  def updateEnterprise (period: String, id: String, newValuesAsMap: String) = ???
//  updateEnterpriseVariableValues(YearMonth referencePeriod, String enterpriseReferenceNumber, Map<String, String> newVariableValues)


}
