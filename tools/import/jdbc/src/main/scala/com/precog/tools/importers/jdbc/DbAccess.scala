package com.precog.tools.importers.jdbc

import java.sql._
import Datatypes._
import scalaz.{StreamT, Id}

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
    val stmt = conn.prepareStatement(query)
    getColumns(stmt)
  }

  def getColumns(stmt:PreparedStatement):IndexedSeq[Column]={
    val tblMetaData = stmt.getMetaData
    val count=columnCount(stmt)
    for ( i <- 1 to count) yield Column(tblMetaData.getColumnName(i),Table(tblMetaData.getTableName(i)))
  }

  //warning: this serves the purpose but it doesn't
  private def rsIterator[T](rs:ResultSet)(f:ResultSet => T) = new Iterator[T] {
    def hasNext = rs.next()
    def next():T = f(rs)
  }

  def rsStreamT[T](rs:ResultSet)(f:ResultSet => T)=StreamT.unfold(rs)( (rs:ResultSet) => if (rs.next()) { Some(f(rs),rs)} else None )

  def oneColumnRs(rs:ResultSet) = rsIterator(rs)(rs=> rs.getString(1))
  def tables(rs:ResultSet) = rsIterator(rs)(rs=> Table(rs.getString("TABLE_NAME")))
  def columns(rs:ResultSet) = rsIterator(rs)(rs=> Column(rs.getString("COLUMN_NAME"), Table(rs.getString("TABLE_NAME"))))
  def relationshipDesc(rs:ResultSet) = rsIterator(rs)(
    rs=> PkFkRelation(
      Key(Table(rs.getString("PKTABLE_NAME")),rs.getString("PKCOLUMN_NAME")),
      Key(Table(rs.getString("FKTABLE_NAME")),rs.getString("FKCOLUMN_NAME")),
      rs.getInt("KEY_SEQ")
    )
  )

  def allSet(rs:ResultSet) = {
    val count= rs.getMetaData.getColumnCount
    rsIterator(rs)(rs=> for ( i <- 1 to count) yield rs.getString(i) )
  }
}
