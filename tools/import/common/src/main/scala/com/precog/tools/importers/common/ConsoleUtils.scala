package com.precog.tools.importers.common

import annotation.tailrec

/**
 * User: gabriel
 * Date: 1/25/13
 */
object ConsoleUtils {

  @tailrec
  def selectSet[T](label:String, available: Seq[T], selected: Seq[T]=List()): Seq[T] =
    if (available.isEmpty) selected
    else {

      println("Available %ss:".format(label))
      println(present(available))

      println("Selected %ss:".format(label))
      println(present(selected))

      println("Select %ss by entering its number or name, 0 to select all, enter to continue: ".format(label))

      val selIdx = readLine()
      selIdx match {
        case "" => selected
        case ParseInt(0) => available
        case ParseInt(x) if x<=available.size => {
          val elem:T = available(x - 1)
          selectSet(label,available.filterNot(_==elem), selected:+elem)
        }
        case s:String if (available.exists(_.toString == s)) => {
          val elem:T =available.find(_.toString == s).get
          selectSet(label,available.filterNot(_==elem), selected:+elem)
        }
        case _ => selectSet(label,available, selected)
      }
    }

  @tailrec
  def selectOne[T](label:String, available: Seq[T]): T ={

      println("Available %ss:".format(label))
      println(present(available))

      println("Select one %s by entering its number or name: ".format(label))

      val selIdx = readLine()
      selIdx match {
        case ParseInt(x) if x<=available.size => available(x - 1)
        case s:String  => available.find(_.toString == s) match {
                            case Some(t) => t
                            case None => selectOne(label,available)
                          }
        case _ => selectOne(label,available)
      }
  }

  def present[T](arr:Seq[T])= arr.zipWithIndex.map({ case (a, b) => (b+1) + ":" + a }).mkString(", ")


}
