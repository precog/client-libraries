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

case class Trackable[Json: JsonImplementation](
  path: Path, name: String, properties: Json, 
  rollup: Boolean = false, 
  tags: Set[Tag[Json]] = Set.empty[Tag[Json]], 
  count: Option[Int] = None, 
  headers: Map[String, String] = Map.empty)

abstract class ReportGridTrackingClient[Json](jsonImplementation: JsonImplementation[Json]) {
  def track(trackable: Trackable[Json]): Unit = track(
    trackable.path, trackable.name, trackable.properties, trackable.rollup, trackable.tags, trackable.count, trackable.headers)

  def track(path: Path, name: String, 
            properties: Json = jsonImplementation.EmptyObject, 
            rollup: Boolean = false, 
            tags: Set[Tag[Json]] = Set.empty[Tag[Json]], 
            count: Option[Int] = None, 
            headers: Map[String, String] = Map.empty): Unit
}

/** Creates a new ReportGrid API based on the specified token, and implicitly
 * the Json implementation and HTTP client.
 */
class ReportGridClient[Json](conf: ReportGridConfig)(implicit val jsonImplementation: JsonImplementation[Json]) 
extends ReportGridTrackingClient[Json](jsonImplementation) with QueryTerms[Json] with Results[Json] {
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
  def track(path: Path, name: String, properties: Json = EmptyObject, rollup: Boolean = false, tags: Set[Tag[Json]] = Set.empty, count: Option[Int] = None, headers: Map[String, String] = Map.empty): Unit = {
    val paths = if (rollup) path :: path.ancestors else path :: Nil
    val data = JsonObject(name -> JsonObject(tags.map(_.toJsonField)(collection.breakOut): _*).merge(properties))

    paths.foreach { path =>
      AnalyticsServer.post("vfs" + path.toString, data, headers)
    }
  }

  /**
   * Utility method for the "listX" variants.
   */
  private def list(path: Path, property: Option[Property] = None): List[Either[Path, Property]] = {
    AnalyticsServer.get("vfs" + path.toString + property.map(_.value).getOrElse("")).deserialize[List[String]].collect {
      case path: String if (path.endsWith("/"))   => Left(Path(path))
      case prop: String if (prop.startsWith(".")) => Right(Property(prop))
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

  /** Selects either a count or  time series of either properties, or properties having certain
   * values. 
   *
   * {{{
   * select(Minute(start, end)).of(".subscription.features").from("/customers/jdoe/blogs/1")
   *
   * select(Year(start, end)).from("/customers/jdoe/blogs/1").where {
   *   ".click.browser" === "Firefox".serialize[Json]
   * }
   * }}}
   */
  def select(selection: Selection) = new {
    private def url(baseUrl: String) = selection match {
      case Series(start, end) => baseUrl + "?start="+start.getTime+"&end="+end.getTime
      case Count => baseUrl
    }

    def of(property: Property) = new {
      def from(path: Path): SelectionResult = {
        val baseUrl = "vfs" + path.toString + property.value + "/" + selection.pathFragment
        val result = AnalyticsServer.get(url(baseUrl))

        selection match {
          case Count => CountResult(result.deserialize[Long])
          case _ => SeriesResult(result.deserialize[Seq[(Json, Long)]])
        }
      }
    }

    def from(path: Path) = new {
      def where(condition: Condition): SelectionResult = {
        val baseUrl  = "search"
        val data = JsonObject(
          ("select" -> selection.pathFragment.serialize[Json]) ::
          ("from"   -> path.toString.serialize[Json]) ::
          ("where"  -> condition.serialize[Json]) :: Nil
        )

        val result = AnalyticsServer.post(url(baseUrl), data)

        selection match {
          case Count => CountResult(result.deserialize[Long])
          case _ => SeriesResult(result.deserialize[Seq[(Json, Long)]])
        }
      }
    }
  }

  /**
   * intersect(Counts) of top(10)(".click")
   */
  def intersect(selection: Selection) = new DimensionBuilder(selection, Nil)

  class DimensionBuilder(selection: Selection, dimensions: List[Dimension]) {
    def top(limit: Int) = new {
      def of(property: Property) = Dimensions(selection, Dimension(High, limit, property) :: dimensions)
    }

    def bottom(limit: Int) = new {
      def of(property: Property) = Dimensions(selection, Dimension(Low, limit, property) :: dimensions)
    }
  }

  case class Dimensions(selection: Selection, dims: List[Dimension]) {
    private def url(baseUrl: String) = selection match {
      case Series(start, end) => baseUrl + "?start="+start.getTime+"&end="+end.getTime
      case Count => baseUrl
    }

    def and = new DimensionBuilder(selection, dims)
    def from(path: Path) = {
      val baseUrl = "intersect"
      val data = JsonObject(
        ("select" -> selection.pathFragment.serialize[Json]) ::
        ("from" -> path.toString.serialize[Json]) ::
        ("properties" -> JsonArray(dims.map(_.serialize[Json]))) :: Nil
      )

      val results = AnalyticsServer.post(url(baseUrl), data)

      selection match {
        case Count => results.deserialize[(Seq[Json], CountResult)]
        case _ => results.deserialize[(Seq[Json], SeriesResult)]
      }
    }
  }

  def tokens(): List[String] = AnalyticsServer.get("tokens/").deserialize[List[String]]
  def token(tokenId: String): Token = AnalyticsServer.get("tokens/" + tokenId).deserialize[Token]

  def newToken(newToken: Token): String = AnalyticsServer.post("tokens/", newToken.serialize[Json]).deserialize[String]
  def deleteToken(tokenId: String): Unit = AnalyticsServer.delete("tokens/" + tokenId)
}
