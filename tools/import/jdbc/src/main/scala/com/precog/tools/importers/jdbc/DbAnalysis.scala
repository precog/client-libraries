package com.precog.tools.importers.jdbc

import java.sql._
import scala.Array
import DbAccess._

/**
 * User: gabriel
 * Date: 11/30/12
 */
object DbAnalysis{



  def isSelected(s: Seq[Table], r: Join) = s.contains(r.refKey.table)
  def filterNotSelected(relations: Seq[Join], selectedTables: Seq[Table]) = relations.filter(r => !isSelected(selectedTables, r))


  def findTables(metadata: DatabaseMetaData, cat: String, tableName: => String): Array[Table] = {
    tables(metadata.getTables(cat, null, tableName, Array("TABLE"))).toArray
  }


  //finds the PK column from a list of columns
  def findPkColumn(allIdColumns:List[Column], fkName:String):Option[Column]={
    allIdColumns.find(c => fkName.contains(c.table.name) )
  }

  def countMatches(conn:Connection, pk: Column, fk: Column)={
    val rs:ResultSet=conn.createStatement().executeQuery(
      "select count(*) from %s as t1, %s as t2 where t1.%s=t2.%s"
        .format(pk.table,fk.table,pk.name,fk.name))
    if (!rs.next()) 0 else rs.getInt(1) //might be better with a "where exists"
  }

  def getDeclaredRelationships(metadata:DatabaseMetaData, cat:String, table: Table):Set[Join] = {
    val exported= relationshipDesc(metadata.getExportedKeys(cat, null, table.name)).map( r => Join(r.pk.columnName,r.fk,true)).toSet
    val imported= relationshipDesc(metadata.getImportedKeys(cat, null, table.name)).map( r => Join(r.fk.columnName,r.pk,false)).toSet
    //filter reflexive imported relations already exported (prevents duplicate self relations
    val importedFiltered= imported.filterNot(pr => exported.contains(Join(pr.refKey.columnName, Key(table,pr.baseColName),false) ))
    exported.union(importedFiltered)
  }


  def getInferredRelationships(conn:Connection, metadata:DatabaseMetaData, catalog:String, table:Table, idPattern:String, sample:Boolean = true):Set[Join]={
    val userTables=findTables(metadata,null,null)
    val allIdColumns=columns(metadata.getColumns(catalog,null,null,'%'+idPattern)).toList.filter( c=> userTables.contains(c.table))
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
}
