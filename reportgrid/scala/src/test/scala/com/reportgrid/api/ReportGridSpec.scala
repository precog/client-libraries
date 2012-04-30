package com.reportgrid.api

import org.specs2.specification.{Fragments,Step}
import org.specs2.mutable.Specification

import _root_.blueeyes.json.JsonAST._
import _root_.blueeyes.json.JsonParser._
import com.reportgrid.api._
import rosetta.json.blueeyes._

object EchoHttpClient extends HttpClient[String] {
  def request(method: String, url: String, content: Option[String], headers: Map[String, String] = Map.empty[String, String]): String = content.getOrElse("")
}

class TrackingSpec extends Specification {
  val trackingClient = new ReportGridClient(ReportGridConfig(Token.Test, Server.Dev, new HttpClientApache))
  val baseTrackPath = Path("/scala/track")
  val rollupBasePath = Path("/scala/rollup")
  val rollupChildPath = rollupBasePath / Path("child")

  val testEvent = parse("{ \"testval\" : 1 }")

  def postRollupEvents = {
    // Rollup tracks
    trackingClient.track(rollupChildPath, "scalaevent", testEvent, FullRollup)

    Thread.sleep(30000)
  }

  override def map(fs : => Fragments) = Step(postRollupEvents) ^ fs

  "TrackingClient" should {
    "perform normal tracks" in {
      trackingClient.track(baseTrackPath, "scalaevent", testEvent)
      success
    }

    "perform rollup tracks" in {
      import trackingClient._
      val baseCount = trackingClient.select(Count).of(".scalaevent").from(rollupBasePath) match {
        case CountResult(count) => count
        case other => failure("Count returned non-count: " + other)
      }

      val childCount = trackingClient.select(Count).of(".scalaevent").from(rollupChildPath) match {
        case CountResult(count) => count
        case other => failure("Count returned non-count: " + other)
      }

      baseCount must_!= 0
      baseCount must_== childCount
    }
  }
}

class ReportGridSpec extends Specification {
  
  "intersection queries" should {
    "correctly generate a query message" in {
      val api = new ReportGridClient(ReportGridConfig(Token.Test,Server.Dev,EchoHttpClient))

      import api._

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
    }.pendingUntilFixed(": !! Scala client code is out-of-date")
  }
}


// vim: set ts=4 sw=4 et:
