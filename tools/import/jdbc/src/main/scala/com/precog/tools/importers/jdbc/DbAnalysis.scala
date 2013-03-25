package com.precog.tools.importers.jdbc

import java.sql._
import scala.Array
import DbAccess._
import Datatypes._

/**
 * User: gabriel
 * Date: 11/30/12
 */
object DbAnalysis{



  def isSelected(s: Seq[Table], r: Join) = s.contains(r.refKey.table)
  def filterNotSelected(relations: Seq[Join], selectedTables: Seq[Table]) = relations.filter(r => !isSelected(selectedTables, r))


  def findTables(metadata: DatabaseMetaData, oCat: Option[String], tableName: => Option[String]): Array[Table] = {
    val cat= oCat.getOrElse(null)//toNullUppercase(oCat)
    val tableNm= tableName.map(_.toUpperCase).getOrElse(null)
    tables(metadata.getTables(cat, null, tableNm, Array("TABLE"))).toArray
  }


  //finds the PK column from a list of columns
  def findPkColumn(allIdColumns:List[Column], fkName:String):Option[Column]={
    allIdColumns.find(c => fkName.contains(c.table.name) )
  }

  def countMatches(conn:Connection, pk: Column, fk: Column)={
    val rs:ResultSet=conn.createStatement().executeQuery(
      "select count(*) from %s as t1, %s as t2 where t1.%s=t2.%s"
        .format(pk.table.name.toUpperCase,fk.table.name.toUpperCase,pk.name.toUpperCase,fk.name.toUpperCase))
    if (!rs.next()) 0 else rs.getInt(1) //might be better with a "where exists"
  }

  def getDeclaredRelationships(metadata:DatabaseMetaData, oCat:Option[String], table: Table):Set[Join] = {
    val cat=toNullUppercase(oCat)
    val exported= relationshipDesc(metadata.getExportedKeys(cat, null, table.name)).map( r => Join(r.pk.columnName,r.fk,ExportedKey)).toSet
    val imported= relationshipDesc(metadata.getImportedKeys(cat, null, table.name)).map( r => Join(r.fk.columnName,r.pk,ImportedKey)).toSet
    //filter reflexive imported relations already exported (prevents duplicate self relations
    val importedFiltered= imported.filterNot(pr => exported.contains(Join(pr.refKey.columnName, Key(table,pr.baseColName),ImportedKey) ))
    exported.union(importedFiltered)
  }


  def getInferredRelationships(conn:Connection, metadata:DatabaseMetaData, oCatalog:Option[String], table:Table, idPattern:String, sample:Boolean = true):Set[Join]={
    val userTables=findTables(metadata,None,None)
    val catalog= toNullUppercase(oCatalog)
    val allIdColumns=columns(metadata.getColumns(catalog,null,null,'%'+idPattern.toUpperCase)).toList.filter( c=> userTables.contains(c.table))
    val similar=allIdColumns.groupBy(_.name)
    for (
      (fkName, colList) <- similar.toSet;
      pkColumn <- findPkColumn( allIdColumns, fkName).toList;
      column <- colList if ((column.table == table || pkColumn.table == table) && (pkColumn!=column) && (!sample || countMatches(conn,pkColumn,column)>0) )
    ) yield {
      val pk=Key(pkColumn.table,pkColumn.name)
      val fk = Key(column.table,column.name)
      val (start,end,exported)=if (pkColumn.table==table) (pk,fk,true) else (fk,pk,false)
      Join(start.columnName,end,exported)
    }
  }

  def toNullUppercase(oCat:Option[String])=oCat.map(_.toUpperCase).getOrElse(null)
}
