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

sealed trait Selection extends Product {
  def name: String = productPrefix.toLowerCase

  def range: Option[(Date, Date)]

  override def toString = name
}

sealed trait Series extends Selection {
  def range: Option[(Date, Date)]
}
object Series {
  case class Minute(range: Option[(Date, Date)] = None) extends Series
  case class Hour  (range: Option[(Date, Date)] = None) extends Series
  case class Day   (range: Option[(Date, Date)] = None) extends Series
  case class Week  (range: Option[(Date, Date)] = None) extends Series
  case class Month (range: Option[(Date, Date)] = None) extends Series
  case class Year  (range: Option[(Date, Date)] = None) extends Series

  sealed trait SeriesFactory[T <: Series] {
    def apply(range: Option[(Date, Date)]): T

    def between(start: Date, end: Date) = apply(Some((start, end)))

    def All: T = apply(None)
  }

  case object Minute extends SeriesFactory[Minute]
  case object Hour   extends SeriesFactory[Hour]
  case object Day    extends SeriesFactory[Day]
  case object Week   extends SeriesFactory[Week]
  case object Month  extends SeriesFactory[Month]
  case object Year   extends SeriesFactory[Year]
}

case object Count extends Selection {
  def range = None
}

sealed trait Condition[Json] { self =>
  def & (that: Condition[Json]): Condition[Json] = self && that

  def && (that: Condition[Json]): Condition[Json] = Condition.And(self, that)
}
sealed trait ConditionPrimitive[Json] extends Condition[Json]

object Condition {
  def Empty[Json] = Composite[Json](Nil)

  def And[Json](c1: Condition[Json], c2: Condition[Json]): Condition[Json] = {
    (c1, c2) match {
      case (c1 : ConditionPrimitive[Json], c2: ConditionPrimitive[Json]) =>
        Composite[Json](primitives = c1 :: c2 :: Nil)

      case (c1 : ConditionPrimitive[Json], c2: Composite[Json]) =>
        c2.copy(primitives = c1 :: c2.primitives)

      case (c1: Composite[Json], c2 : ConditionPrimitive[Json]) =>
        c1.copy(primitives = c1.primitives :+ c2)

      case (c1: Composite[Json], c2: Composite[Json]) =>
        c1.copy(primitives = c1.primitives ++ c2.primitives)
    }
  }
}

case class Equals[Json](property: Property, value: Json) extends ConditionPrimitive[Json]
case class Composite[Json](primitives: List[ConditionPrimitive[Json]]) extends Condition[Json]

class Property private (val value: String) {
  def === [Json](json: Json): Equals[Json] = Equals[Json](this, json)

  override def equals(any: Any): Boolean = any match {
    case that: Property => this.value == that.value

    case _ => false
  }

  override def hashCode = value.hashCode

  override def toString = value
}
object Property {
  implicit def string2Property(string: String): Property = Property(string)

  def apply(suffix: String): Property = {
    val value = if (suffix.startsWith(".")) suffix else "." + suffix

    new Property(value)
  }
}
