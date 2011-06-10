package com.reportgrid.api

import org.specs._
import _root_.blueeyes.core.service._
import _root_.blueeyes.core.service.HttpClient._
import _root_.blueeyes.json.JsonAST._
import com.reportgrid.api.blueeyes._
import com.reportgrid.analytics.{Token => AToken}

object EchoHttpClient extends HttpClient[String] {
  def request(method: String, url: String, content: Option[String], headers: Map[String, String] = Map.empty[String, String]): String = content.getOrElse("")
}

class ReportGridSpec extends Specification {
  "intersection queries" should {
    "correctly generate a query message" in {
      val api = new BlueEyesReportGridClient {
        val tokenId = AToken.Test.tokenId
        val config = ReportGridConfig.Local
        val httpClient = EchoHttpClient
      }

      api.intersect(Count).top(10).of(".test").and.bottom(10).of(".test2").from("/foo/") must_== JObject(List(
          JField("select",JString("count")),
          JField("from",JString("/foo/")),
          JField("properties", JArray(List(
            JObject(List(
              JField("property",JString(".test2")),
              JField("limit",JInt(10)),
              JField("order",JString("ascending")))
            ),
            JObject(List(
              JField("property",JString(".test")),
              JField("limit",JInt(10)),
              JField("order",JString("descending")))
            )))
          )
      ))
    }
  }
}


// vim: set ts=4 sw=4 et:
