package com.precog.tools.importers.jdbc

import java.sql._

/**
 * User: gabriel
 * Date: 11/30/12
 */
object DbAccess {
  def columnCount(stmt:PreparedStatement)=stmt.getMetaData.getColumnCount

  def getColumns(conn:Connection, query:String):IndexedSeq[String]={
    //use a prepared statement to get the metadata
    //might have to revert back to query & result set
    val stmt = conn.prepareStatement(query)
    getColumns(stmt)
  }

  def getColumns(stmt:PreparedStatement):IndexedSeq[String]={
    val tblMetaData = stmt.getMetaData
    val count=columnCount(stmt)
    for ( i <- 1 to count) yield ( tblMetaData.getColumnName(i))
  }

  def rsIterator[T](rs:ResultSet)(f:ResultSet => T) = new Iterator[T] {
    def hasNext = rs.next()
    def next():T = f(rs)
  }

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
