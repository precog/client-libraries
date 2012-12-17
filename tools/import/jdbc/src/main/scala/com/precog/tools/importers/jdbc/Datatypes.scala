package com.precog.tools.importers.jdbc

/**
 * User: gabriel
 * Date: 11/24/12
 */
object Datatypes{
  case class Table(name:String){ override val toString=name }
  case class Column(name:String, table:Table){ override val toString="%s.%s".format(table,name) }
  case class Key(table:Table, columnName:String){ override val toString="%s.%s".format(table,columnName) }

  case class PkFkRelation(pk:Key,fk:Key, seq:Int){
    override val toString="%s(pk)->%s(fk) (%s)".format(pk,fk,seq)
  }

  case class Join(baseColName:String, refKey:Key,exported:Boolean){
    override val toString = "%s%s%s".format(baseColName, if (exported) "<-*" else "*->", refKey )
  }

  val ExportedKey=true
  val ImportedKey=false
}
