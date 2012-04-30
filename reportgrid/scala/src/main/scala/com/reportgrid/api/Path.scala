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

class Path private (private val path_ : String) {
  val path = cleanPath(path_)

  lazy val elements = path.split("/")

  def + (that: Path) = this / that

  def - (that: Path): Path = if (this.path.startsWith(that.path)) {
    new Path(path.substring(that.path.length))
  } else sys.error("This path is not a descendent of that path: this = " + this.toString + ", that = " + that.toString)

  def / (that: Path) = new Path(this.path + "/" + that.path)

  def parent: Option[Path] = path.split("/").reverse.toList match {
    case Nil      => None
    case x :: Nil => Some(new Path("/"))
    case x :: xs  => Some(new Path(xs.reverse.mkString("/")))
  }

  def ancestors: List[Path] = {
    val parentList = parent.toList

    parentList ++ parentList.flatMap(_.ancestors)
  }

  def parentChildRelations: List[(Path, Path)] = {
    val a = ancestors

    a.zip(this :: a)
  }

  override def equals(that: Any) = that match {
    case that: Path => this.path == that.path

    case _ => false
  }

  override def hashCode = path.hashCode

  override def toString = path

  private def cleanPath(string: String): String = ("/" + string + "/").replaceAll("/+", "/")
}

object Path {
  val Root = Path("/")

  implicit def stringToPath(string: String): Path = apply(string)

  def apply(path: String): Path = new Path(path)

  def unapply(path: Path): Option[String] = Some(path.path)
}
