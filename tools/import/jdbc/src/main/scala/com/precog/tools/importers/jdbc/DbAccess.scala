package com.precog.tools.importers.jdbc

import java.sql._
import Datatypes._
import scalaz.StreamT

/**
 * User: gabriel
 * Date: 11/30/12
 */
object DbAccess {
  def columnCount(stmt:PreparedStatement)=stmt.getMetaData.getColumnCount

  def getColumns(conn:Connection, table:Table):IndexedSeq[Column]={
    getColumns(conn,"select * from %s".format(table.name.toUpperCase))
  }

  def getColumns(conn:Connection, query:String):IndexedSeq[Column]={
    //use a prepared statement to get the metadata
    println(query)
    val stmt = conn.prepareStatement(query)
    getColumns(stmt)
  }

  def getColumns(stmt:PreparedStatement):IndexedSeq[Column]={
    val tblMetaData = stmt.getMetaData
    val count=columnCount(stmt)
    for ( i <- 1 to count) yield Column(tblMetaData.getColumnName(i),Table(tblMetaData.getTableName(i)))
  }



  def rsList[T](rs:ResultSet)(f:ResultSet => T)={
    //warning: this serves the purpose but it not a well behaved iterator.
    //In particular, a call to hasNext, advances the resultSet
    //it works in this context, because we just call it with "toList"
    def rsIterator(rs:ResultSet)(f:ResultSet => T) = new Iterator[T] {
      def hasNext = rs.next()
      def next():T = f(rs)
    }
    rsIterator(rs)(f).toList
  }

  def rsStreamT[T](rs:ResultSet)(f:ResultSet => T)=StreamT.unfold(rs)( (rs:ResultSet) => if (rs.next()) { Some(f(rs),rs)} else None )

  def oneColumnRs(rs:ResultSet) =rsList(rs)(rs=> rs.getString(1))
  def tables(rs:ResultSet) = rsList(rs)(rs=> Table(rs.getString("TABLE_NAME")))
  def columns(rs:ResultSet) = rsList(rs)(rs=> Column(rs.getString("COLUMN_NAME"), Table(rs.getString("TABLE_NAME"))))
  def relationshipDesc(rs:ResultSet) = rsList(rs)(
    rs=> PkFkRelation(
      Key(Table(rs.getString("PKTABLE_NAME")),rs.getString("PKCOLUMN_NAME")),
      Key(Table(rs.getString("FKTABLE_NAME")),rs.getString("FKCOLUMN_NAME")),
      rs.getInt("KEY_SEQ")
    )
  )
}
