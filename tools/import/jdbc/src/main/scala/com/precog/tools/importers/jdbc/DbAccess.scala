package com.precog.tools.importers.jdbc

import java.sql._
import Datatypes._
import scalaz.StreamT
import annotation.tailrec
import scalaz.effect.IO
import org.slf4j.LoggerFactory

/**
 * User: gabriel
 * Date: 11/30/12
 */
object DbAccess {

  private lazy val logger = LoggerFactory.getLogger("com.precog.tools.importers.jdbc.DbAccess")

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



  def rsList[T](rs:ResultSet)(f:ResultSet => T)={

    @tailrec
    def buildList(rs:ResultSet, acc:List[T]=Nil):List[T]=
      if (rs.next()) buildList(rs, f(rs)::acc)
      else acc

    buildList(rs).reverse
  }

  def rsStreamT[T](rs:ResultSet)(f:ResultSet => T)=StreamT.unfoldM(rs)(
    (rs:ResultSet) => IO( { val d=if (rs.next()) { Some(f(rs),rs)} else None; logger.info("read stream = %s".format(d)); d }))

  def rsStream[T](rs:ResultSet)(f:ResultSet => T):Stream[T] = if (rs.next) f(rs) #:: rsStream(rs)(f) else Stream.empty

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
