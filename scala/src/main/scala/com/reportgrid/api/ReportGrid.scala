/* Copyright (C) 2011 by ReportGrid, Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * No portion of this Software shall be used in any application which does not
 * use the ReportGrid platform to provide some subset of its functionality.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.reportgrid.api

import java.util.Date

import rosetta.json.JsonImplementation
import rosetta.io._

/** Creates a new ReportGrid API based on the specified token, and implicitly
 * the Json implementation and HTTP client.
 */
class ReportGridAbstract[Json](tokenId: String)(implicit val jsonImplementation: JsonImplementation[Json], httpClient: HttpClient[String]) extends Serialization[Json] {
  import jsonImplementation._

  val ReportGridServicesRoot = "http://api.reportgrid.com/services/"

  val AnalyticsServer: HttpClient[Json] = httpClient.url(ReportGridServicesRoot + "analytics/v0/").query("tokenId", tokenId).contentType("application/json")

  /** Tracks the event with the specified name and properties.
   *
   * @param path        The path to track the event under.
   * @param name        The name of the event.
   * @param properties  The JSON properties of the event.
   * @param rollup      Whether or not the event rolls up to parent paths.
   * @param timestamp   An optional timestamp denoting the time at which the event occurred.
   * @param count       An optional count of how many times the event occurred.
   */
  def track(path: Path, name: String, properties: Json = EmptyObject, rollup: Boolean = false, timestamp: Option[Date] = None, count: Option[Int] = None): Unit = {
    val timestampM = timestamp.map { timestamp =>
      "timestamp" -> timestamp.getTime.serialize[Json]
    }

    val countM = count.map { count =>
      "count" -> count.serialize[Json]
    }

    AnalyticsServer.post("vfs" + path.toString,
      JsonObject(
        List[(String, Json)]("events" -> properties) ++ timestampM.toList ++ countM.toList
      )
    )

    if (rollup) {
      path.parent.map { parent =>
        track(parent, name, properties, rollup, timestamp, count)
      }
    }
  }

  /** Lists all children of the specified path and property.
   *
   * @param path      The path, such as "/videos/1"
   * @param property  The property, such as ".engagement.duration"
   */
  def list(path: Path, property: Property): List[String] = list(path, Some(property))

  /**
   * Lists all children of the specified path, both path children and event
   * children.
   */
  def list(path: Path): List[String] = list(path, None)

  private def list(path: Path, property: Option[Property] = None): List[String] = {
    AnalyticsServer.get("vfs" + path.toString + property.map(_.value).getOrElse("")).deserialize[List[String]]
  }

  /** Retrieves all values acquired by the specified property, over all
   * time periods. This list could potentially be quite large.
   *
   * @property The event property, such as ".withdrawal.amount".
   */
  def valuesOf(property: Property) = new {
    def from(path: Path): List[Json] = {
      AnalyticsServer.get("vfs" + path.toString + property.value + "/values/").deserialize[List[Json]]
    }
  }

  /** Selects a time series of either properties, or properties having certain
   * values. Optionally, the range of the time series may be limited to a
   * specified date range.
   *
   * {{{
   * select(Minute()).of(".subscription.features").from("/customers/jdoe/blogs/1")
   *
   * select(Year(start, end)).from("/customers/jdoe/blogs/1").where {
   *   ".click.browser" === "Firefox".serialize[Json]
   * }
   * }}}
   */
  def select(selection: Series) = new {
    def of(property: Property) = new {
      def from(path: Path): List[(Date, Long)] = {
        val url     = "vfs" + path.toString + property.value + "/series/" + selection.name
        val headers = headersFrom(selection)

        AnalyticsServer.get(url, headers).deserialize[List[(Date, Long)]]
      }
    }

    def from(path: Path) = new {
      def where(condition: Condition[Json]): List[(Date, Long)] = {
        val url     = "search"
        val headers = headersFrom(selection)

        AnalyticsServer.post(url,
          JsonObject(
            ("select" -> ("series/" + selection.name).serialize[Json]) ::
            ("from"   -> path.toString.serialize[Json]) ::
            ("where"  -> condition.serialize[Json]) :: Nil
          ), headers
        ).deserialize[List[(Date, Long)]]
      }
    }
  }

  /** Selects the count of either properties, or properties having certain
   * values.
   *
   * {{{
   * select(Count).of(".click").from("/customers/jdoe/blogs/1")
   *
   * select(Count).from("/customers/jdoe/blogs/1").where {
   *   ".type" === "impression".serialize[Json]
   * }
   * }}}
   */
  def select(selection: Count.type) = new {
    def of(property: Property) = new {
      def from(path: Path): Long = {
        val url     = "vfs" + path.toString + property.value + "/" + selection.name
        val headers = headersFrom(selection)

        AnalyticsServer.get(url, headers).deserialize[Long]
      }
    }

    def from(path: Path) = new {
      def where(condition: Condition[Json]): Long = {
        val url     = "search"
        val headers = headersFrom(selection)

        AnalyticsServer.post(url,
          JsonObject(
            ("select" -> "count".serialize[Json]) ::
            ("from"   -> path.toString.serialize[Json]) ::
            ("where"  -> condition.serialize[Json]) :: Nil
          ), headers
        ).deserialize[Long]
      }
    }
  }

  private def headersFrom(selection: Selection): Map[String, String] = {
    Map.empty[String, String] ++ ((selection match {
      case Count => Nil

      case series: Series => series.range.toList.flatMap { tuple =>
        val (start, end) = tuple

        ("Range" -> ("time=" + start.getTime.toString + end.getTime.toString)) :: Nil
      }
    }): Iterable[(String, String)])
  }

  def tokens: List[String] = AnalyticsServer.get("vfs/tokens/").deserialize[List[String]]

  def newToken(newToken: Token): Unit = {
    AnalyticsServer.post("tokens/", newToken.serialize[Json])
  }

  def token(tokenId: String): Token = {
    AnalyticsServer.get("tokens/" + tokenId).deserialize[Token]
  }

  def deleteToken(tokenId: String): Unit = AnalyticsServer.delete("vfs/tokens/" + tokenId)
}
