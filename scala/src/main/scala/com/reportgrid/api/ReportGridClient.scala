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

import scala.annotation.tailrec

import rosetta.json.JsonImplementation
import rosetta.io._

case class Trackable[Json: JsonImplementation](path: Path, name: String, properties: Json, rollup: Boolean = false, timestamp: Option[Date] = None, count: Option[Int] = None, headers: Map[String, String] = Map.empty)

abstract class ReportGridTrackingClient[Json](jsonImplementation: JsonImplementation[Json]) {
  def track(trackable: Trackable[Json]): Unit = track(trackable.path, trackable.name, trackable.properties, trackable.rollup, trackable.timestamp, trackable.count)
  def track(path: Path, name: String, properties: Json = jsonImplementation.EmptyObject, rollup: Boolean = false, timestamp: Option[Date] = None, count: Option[Int] = None, headers: Map[String, String] = Map.empty): Unit
}


/** Creates a new ReportGrid API based on the specified token, and implicitly
 * the Json implementation and HTTP client.
 */
class ReportGridClient[Json](conf: ReportGridConfig)(implicit val jsonImplementation: JsonImplementation[Json]) extends ReportGridTrackingClient[Json](jsonImplementation) {
  val serializer = new Serialization(jsonImplementation)

  import jsonImplementation._
  import serializer._

  lazy val AnalyticsServer: HttpClient[Json] =  conf.httpClient.url(conf.server.analyticsRootUrl).
                                                query("tokenId", conf.tokenId).
                                                contentType("application/json")

  /** Tracks the event with the specified name and properties.
   *
   * @param path        The path to track the event under.
   * @param name        The name of the event.
   * @param properties  The JSON properties of the event.
   * @param rollup      Whether or not the event rolls up to parent paths.
   * @param timestamp   An optional timestamp denoting the time at which the event occurred.
   * @param count       An optional count of how many times the event occurred.
   */
  def track(path: Path, name: String, properties: Json = EmptyObject, rollup: Boolean = false, timestamp: Option[Date] = None, count: Option[Int] = None, headers: Map[String, String] = Map.empty): Unit = {
    val timestampM = timestamp.map { timestamp =>
      "timestamp" -> timestamp.getTime.serialize[Json]
    }

    val countM = count.map { count =>
      "count" -> count.serialize[Json]
    }

    val paths = if (rollup) path :: path.ancestors else path :: Nil

    val data = JsonObject(
      List[(String, Json)]("events" -> JsonObject(name -> properties)) ++ timestampM.toList ++ countM.toList
    )

    paths.foreach { path =>
      AnalyticsServer.post("vfs" + path.toString, data, headers)
    }
  }

  /** Lists all child properties of the property in the specified path.
   *
   * @param path      The path, such as "/videos/1"
   * @param property  The property, such as ".engagement.duration"
   */
  def listChildProperties(path: Path, property: Property): List[Property] = list(path, Some(property)).collect {
    case Right(prop) => prop
  }

  /** Lists all child properties of the specified path.
   *
   * @param path      The path, such as "/videos/1"
   */
  def listChildProperties(path: Path): List[Property] = list(path, None).collect {
    case Right(prop) => prop
  }

  /**
   * Lists all path children of the specified path.
   */
  def listChildPaths(path: Path): List[Path] = list(path, None).collect {
    case Left(path) => path
  }

  /** Retrieves all values acquired by the specified property, over all
   * time periods. This list could potentially be quite large.
   *
   * {{{
   * valuesOf(".withdrawal.amount").from("/customers/joe/")
   * }}}
   */
  def valuesOf(property: Property) = new {
    def from(path: Path): List[Json] = {
      val url = "vfs" + path.toString + property.value + "/values/"
      AnalyticsServer.get(url).deserialize[List[Json]]
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
      def from(path: Path): SelectionResult = {
        val url     = "vfs" + path.toString + property.value + "/" + selection.pathFragment
        val headers = headersFrom(selection)
        AnalyticsServer.get(url, headers).deserialize[SelectionResult]
      }
    }

    def from(path: Path) = new {
      def where(condition: Condition[Json]): SelectionResult = {
        val url  = "search"
        val data = JsonObject(
          ("select" -> selection.pathFragment.serialize[Json]) ::
          ("from"   -> path.toString.serialize[Json]) ::
          ("where"  -> condition.serialize[Json]) ::
          rangeProperties(selection)
        )

        AnalyticsServer.post(url, data).deserialize[SelectionResult]
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
        AnalyticsServer.get(url).deserialize[Long]
      }
    }

    def from(path: Path) = new {
      def where(condition: Condition[Json]): Long = {
        val url  = "search"
        val data = JsonObject(
          ("select" -> selection.pathFragment.serialize[Json]) ::
          ("from"   -> path.toString.serialize[Json]) ::
          ("where"  -> condition.serialize[Json]) :: Nil
        )

        AnalyticsServer.post(url, data).deserialize[Long]
      }
    }
  }

  /**
   * intersect(Counts) of top(10)(".click")
   */
  def intersect(selection: Selection) = new DimensionBuilder(selection, Nil)

  def tokens(): List[String] = AnalyticsServer.get("tokens/").deserialize[List[String]]

  def newToken(newToken: Token): String = {
    AnalyticsServer.post("tokens/", newToken.serialize[Json]).deserialize[String]
  }

  def token(tokenId: String): Token = {
    AnalyticsServer.get("tokens/" + tokenId).deserialize[Token]
  }

  def deleteToken(tokenId: String): Unit = AnalyticsServer.delete("tokens/" + tokenId)

  private def headersFrom(selection: Selection): Map[String, String] = Map(
    selection.range.toList.map { case (start, end) => 
      "Range" -> ("time=" + start.getTime.toString + "-" + end.getTime.toString)
    }: _*
  )
  
  private def rangeProperties(selection: Selection) = selection.range.toList.flatMap {
    case (start, end) => 
      ("start" -> start.serialize[Json]) ::
      ("end"   -> end.serialize[Json])   :: Nil
  }

  private def list(path: Path, property: Option[Property] = None): List[Either[Path, Property]] = {
    AnalyticsServer.get("vfs" + path.toString + property.map(_.value).getOrElse("")).deserialize[List[String]].collect {
      case path: String if (path.endsWith("/"))   => Left(Path(path))
      case prop: String if (prop.startsWith(".")) => Right(Property(prop))
    }
  }

  sealed trait Bias
  case object High extends Bias
  case object Low extends Bias
  object Bias {
    implicit def serializer: JsonSerializer[Bias] = new JsonSerializer[Bias] {
      def serialize(bias: Bias) = bias match {
        case High => "descending".serialize[Json]
        case Low  => "ascending".serialize[Json]
      }

      def deserialize(json: Json) = json.deserialize[String] match {
        case "descending" => High
        case "ascending"  => Low
      }
    }
  }

  class DimensionBuilder(selection: Selection, dimensions: List[Dimension]) {
    def top(limit: Int) = new {
      def of(property: Property) = Dimensions(selection, Dimension(High, limit, property) :: dimensions)
    }

    def bottom(limit: Int) = new {
      def of(property: Property) = Dimensions(selection, Dimension(Low, limit, property) :: dimensions)
    }
  }

  case class Dimension(bias: Bias, limit: Int, property: Property)

  object Dimension {
    implicit def serializer : JsonSerializer[Dimension] = new JsonSerializer[Dimension] {
      def serialize(dimension: Dimension): Json = {
        JsonObject(
          ("property",    dimension.property.toString.serialize[Json])   ::
          ("limit",       dimension.limit.serialize[Json]) ::
          ("order",       dimension.bias.serialize[Json])  :: Nil
        )
      }

      def deserialize(json: Json): Dimension = json match {
        case JsonObject(fields) => (for {
          property <- fields.get("property").map(_.deserialize[String])
          limit    <- fields.get("limit").map(_.deserialize[Int])
          bias     <- fields.get("order").map(_.deserialize[Bias])
        } yield {
          Dimension(bias, limit, property)
        }).get
      }
    }
  }

  case class Dimensions(selection: Selection, dims: List[Dimension]) {
    def and = new DimensionBuilder(selection, dims)
    def from(path: Path) = {
      val url = "intersect"
      val data = JsonObject(
        ("select" -> selection.pathFragment.serialize[Json]) ::
        ("from" -> path.toString.serialize[Json]) ::
        ("properties" -> JsonArray(dims.map(_.serialize[Json]))) ::
        rangeProperties(selection)
      )

      AnalyticsServer.post(url, data)
    }
  }
}
