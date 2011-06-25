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

import rosetta.io._
import rosetta.json.JsonImplementation

abstract class Serialization[Json: JsonImplementation] {
  val jsonImplementation = implicitly[JsonImplementation[Json]]
  import jsonImplementation._

  implicit val PathToJson = new JsonSerializer[Path] {
    def serialize(v: Path): Json = v.toString.serialize

    def deserialize(v: Json): Path = Path(v.deserialize[String])
  }

  implicit val PermissionsToJson = new JsonSerializer[Permissions] {
    def serialize(v: Permissions): Json = JsonObject(Map(
      "read"  -> v.read.serialize[Json],
      "write" -> v.read.serialize[Json],
      "share" -> v.read.serialize[Json]
    ))

    def deserialize(v: Json): Permissions = Permissions(
      read  = v.get(".read").deserialize[Boolean],
      write = v.get(".write").deserialize[Boolean],
      share = v.get(".share").deserialize[Boolean]
    )
  }

  implicit val LimitsToJson = new JsonSerializer[Limits] {
    def serialize(v: Limits): Json = JsonObject(Map(
      "limit" -> v.limit.serialize[Json],
      "depth" -> v.depth.serialize[Json],
      "order" -> v.order.serialize[Json]
    ))

    def deserialize(v: Json): Limits = Limits(
      limit = v.get(".limit").deserialize[Int],
      depth = v.get(".depth").deserialize[Int],
      order = v.get(".order").deserialize[Int]
    )
  }

  implicit val TokenToJson = new JsonSerializer[Token] {
    def serialize(v: Token): Json = JsonObject(Map(
      "path"            -> v.path.serialize[Json],
      "permissions"     -> v.permissions.serialize[Json],
      "expires"         -> v.expires.serialize[Json],
      "limits"          -> v.limits.serialize[Json]
    ))

    def deserialize(v: Json): Token = Token(
      path            = v.get(".path").deserialize[Path],
      permissions     = v.get(".permissions").deserialize[Permissions],
      expires         = v.get(".expires").deserialize[Date],
      limits          = v.get(".limits").deserialize[Limits]
    )
  }

  implicit val ConditionToJson = new JsonSerializer[Condition[Json]] {
    def serialize(v: Condition[Json]): Json = v match {
      case Equals(p, v) => JsonObject(
        (p.value -> v) :: Nil
      )

      case Composite(vs) => JsonObject(
        vs.map { v =>
          v match {
            case Equals(p, v) => p.value -> v
          }
        }
      )
    }

    def deserialize(v: Json): Condition[Json] = v match {
      case JsonObject(fields) =>
        Composite(
          fields.toList.map { field =>
            val (name, value) = field

            Equals(Property(name), value)
          }
        )

      case _ => sys.error("Expected Condition as Json object but found: " + v)
    }
  }
}
