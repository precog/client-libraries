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

import rosetta.json.JsonImplementation
import rosetta.io.SerializerImplicits._
import java.util.Date
import scala.annotation.tailrec

sealed abstract class Tag[Json](implicit val jsonImplementation: JsonImplementation[Json]) {
  def toJsonField: (String, Json)
}

case class TimeTag[Json: JsonImplementation](timestamp: Date) extends Tag[Json] {
  import jsonImplementation._
  def toJsonField: (String, Json) = {
    "#timestamp" -> timestamp.getTime.serialize[Json]
  }
}

case class Location(name: Option[String], path: String)
class LocationTag[Json: JsonImplementation] private (locations: Location*) extends Tag[Json] {
  import jsonImplementation._
  def toJsonField: (String, Json) = {
    "#location" -> {
      if (locations.forall(_.name.isEmpty)) locations.map(_.path).serialize[Json]
      else JsonObject(locations.map(l => (l.name.get, l.path.serialize[Json])): _*)
    }
  }
}

object LocationTag {
  case class TagStructureError(message: String)
  def apply[Json: JsonImplementation](locations: Location*): Either[TagStructureError, LocationTag[Json]] = {
    respectsRefinementRule(locations.toList) 
    .toLeft(if (locations.forall(_.name.isEmpty) || locations.forall(_.name.isDefined)) Right(new LocationTag[Json](locations: _*))
            else Left(TagStructureError("Location name must be present for all locations, or absent for all locations.")))
    .joinRight
  }

  private def respectsRefinementRule(values: List[Location]): Option[TagStructureError] = { 
    @tailrec def parallel(l: List[List[String]], element: Int): Option[TagStructureError] = { 
      val (heads, tails) = l.foldLeft((List.empty[String], List.empty[List[String]])) {
        case ((heads, tails), x :: xs) => (x :: heads, xs :: tails)
        case (ht, Nil) => ht
      }   

      if (heads.isEmpty) None
      else if (heads.distinct.size != 1) 
        Some(TagStructureError("Paths are not parallel; element " + element + " has multiple values: " + heads.distinct.mkString(",")))
      else parallel(tails, element + 1)
    }   

    parallel(values.map(_.path.replaceAll("(^/|/$)*", "").split("/").toList), 1)
  }
}



trait QueryTerms[Json] {
  implicit val jsonImplementation: JsonImplementation[Json]
  import jsonImplementation._

  sealed trait Selection extends Product {
    def pathFragment: String
  }

  sealed abstract class Series(name: String) extends Selection {
    def start: Date
    def end: Date
    override val pathFragment = "series/" + name
  }

  object Series {
    case class Minute(start: Date, end: Date) extends Series("minute")
    case class Hour  (start: Date, end: Date) extends Series("hour")
    case class Day   (start: Date, end: Date) extends Series("day")
    case class Week  (start: Date, end: Date) extends Series("week")
    case class Month (start: Date, end: Date) extends Series("month")
    case class Year  (start: Date, end: Date) extends Series("year")

    def unapply(s: Series) = Some((s.start, s.end))
  }

  case object Count extends Selection {
    override val pathFragment = "count"
  }

  sealed trait Condition { self =>
    def && (that: Condition): Condition = Condition.And(self, that)
  }

  sealed trait ConditionPrimitive extends Condition

  object Condition {
    def Empty = Composite(Nil)

    def And(c1: Condition, c2: Condition): Condition = (c1, c2) match {
      case (c1 : ConditionPrimitive, c2: ConditionPrimitive) =>
        Composite(primitives = c1 :: c2 :: Nil)

      case (c1 : ConditionPrimitive, c2: Composite) =>
        c2.copy(primitives = c1 :: c2.primitives)

      case (c1: Composite, c2 : ConditionPrimitive) =>
        c1.copy(primitives = c1.primitives :+ c2)

      case (c1: Composite, c2: Composite) =>
        c1.copy(primitives = c1.primitives ++ c2.primitives)
    }

    implicit object ConditionToJson extends JsonSerializer[Condition] {
      def serialize(v: Condition): Json = v match {
        case Equals(p, v) => JsonObject((p.value -> v) :: Nil)

        case Composite(vs) => JsonObject(vs.map { case Equals(p, v) => p.value -> v })
      }

      def deserialize(v: Json): Condition = v match {
        case JsonObject(fields) =>
          Composite(
            fields.toList.map { 
              case (name, value) => Equals(Property(name), value)
            }
          )

        case _ => sys.error("Expected Condition as Json object but found: " + v)
      }
    }
  }

  case class Equals(property: Property, value: Json) extends ConditionPrimitive
  case class Composite(primitives: List[ConditionPrimitive]) extends Condition

  class Property private (val value: String) {
    def === (json: Json): Equals = Equals(this, json)

    override def equals(any: Any): Boolean = any match {
      case that: Property => this.value == that.value

      case _ => false
    }

    override def hashCode = value.hashCode

    override def toString = value
  }

  object Property {
    implicit def string2Property(string: String): Property = apply(string)

    def apply(suffix: String): Property = {
      val value = if (suffix.startsWith(".")) suffix else "." + suffix
      new Property(value)
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

  case class Dimension(bias: Bias, limit: Int, property: Property)

  object Dimension {
    implicit object Serializer extends JsonSerializer[Dimension] {
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
}

trait Results[Json] {
  implicit val jsonImplementation: JsonImplementation[Json]
  import jsonImplementation._

  sealed trait SelectionResult

  case class CountResult(count: Long) extends SelectionResult
  object CountResult {
    implicit object Serializer extends JsonSerializer[CountResult] {
      def serialize(result: CountResult): Json = result.count.serialize[Json]

      def deserialize(json: Json): CountResult = json match {
        case JsonLong(value) => CountResult(value)
      }
    }
  }

  case class SeriesResult(series: Seq[(Json, Long)]) extends SelectionResult
  object SeriesResult {
    implicit object Serializer extends JsonSerializer[SeriesResult] {
      def serialize(result: SeriesResult): Json = result.series.serialize[Json]

      def deserialize(json: Json): SeriesResult = json match {
        case JsonArray(values) => SeriesResult(values.map(_.deserialize[(Json, Long)]))
      }
    }
  }
}
