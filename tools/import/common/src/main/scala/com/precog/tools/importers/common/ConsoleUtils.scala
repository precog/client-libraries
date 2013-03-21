package com.precog.tools.importers.common

import annotation.tailrec

/**
 * User: gabriel
 * Date: 1/25/13
 */
object ConsoleUtils {

  @tailrec
  def selectSet[T](label:String, available: Seq[T], selected: Seq[T]=List())(implicit arg0: ClassManifest[T]): Seq[T] =
    if (available.isEmpty) selected
    else {
      val availArray=available.toArray

      println("Available %ss:".format(label))
      println(present(availArray))

      println("Selected %ss:".format(label))
      println(present(selected))

      println("Select a number/enter the name, 0 to select all, or enter to continue: ")

      val selIdx = readLine()
      selIdx match {
        case "" => selected
        case ParseInt(0) => available
        case ParseInt(x) if (x<=available.size) => {
          val elem:T = availArray(x - 1)
          selectSet(label,available.filterNot(_==elem), selected:+elem)
        }
        case s:String if (available.exists(_.toString == s)) => {
          val elem:T =availArray.find(_.toString == s).get
          selectSet(label,available.filterNot(_==elem), selected:+elem)
        }
        case _ => selectSet(label,available, selected)
      }
    }

  def selectOne[T](label:String, available: Seq[T])(implicit arg0: ClassManifest[T]): T ={

      val availArray=available.toArray

      println("Available %ss:".format(label))
      println(present(availArray))

      println("Select a number/enter the name: ")

      val selIdx = readLine()
      selIdx match {
        case ParseInt(x) if (x<=available.size) => availArray(x - 1)
        case s:String if (available.exists(_.toString == s)) => availArray.find(_.toString == s).get
        case _ => selectOne(label,available)
      }
  }

  def present[T](arr:Seq[T])= (1 to arr.length).zip(arr).map(x=>x._1 +":"+ x._2).mkString(", ")


}
