package com.precog.tools.importers

/**
 * User: gabriel
 * Date: 1/25/13
 */
package object common {

  object ParseInt{
    def unapply(s : String) : Option[Int] = try {
      Some(s.toInt)
    } catch {
      case _ : java.lang.NumberFormatException => None
    }
  }


}
