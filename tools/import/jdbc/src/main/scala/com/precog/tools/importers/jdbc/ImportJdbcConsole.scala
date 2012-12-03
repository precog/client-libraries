package com.precog.tools.importers.jdbc

import annotation.tailrec
import java.sql.{Connection, DatabaseMetaData, DriverManager}
import DbAccess._
import DbAnalysis._
import ImportJdbc._


/**
 * User: gabriel
 * Date: 11/16/12
 */
object ImportJdbcConsole {

  def main(args:Array[String]){
    println("Welcome to Precog JDBC import wizard")
    lazy val dbUrl="jdbc:mysql://localhost:3306/" //readLine("Enter database URL:")
    lazy val user="root" //"admin" //readLine("User:"))
    lazy val password = "root"//"admin" //readLine("Password:")
    // use api key and dispatch to call ingest
    lazy val host="http://beta.precog.com" //readLine("ingestion host")   //TODO move to trait ?
    lazy val apiKey="43AB865E-BB86-4F74-A57E-7E8BBD77F2B5"//readLine("API KEY for ingestion")
    lazy val basePath="/0000000457/data" //readLine("Base ingestion path ( /{userId}/....)")
    importJdbc(dbUrl,user,password, host, apiKey, basePath)
  }

  def importJdbc(dbUrl:String, user:String, password: String, host:String, apiKey:String, basePath:String) {

    val conn= getConnection(dbUrl, user, password)
    val metadata= conn.getMetaData
    val cat= getCatalogs(metadata)

    //for querying tables, the connection must be specific to a database
    val connDb= DriverManager.getConnection("%s%s".format(dbUrl,cat),user,password)
    val tqs = getQuery(connDb, metadata, cat)

    tqs.map( tqs => {
      val (table,tDesc,q) = tqs
      val path= "%/%".format(basePath, table)
      println(ingest(connDb,table, q, tDesc, path, host, apiKey))
    })
  }

  def getCatalogs(metadata: DatabaseMetaData): String = {
    println("Catalogs:")
    val catalogs = oneColumnRs(metadata.getCatalogs).toArray
    println(present(catalogs))
    catalogs({println("Select a catalog: ");readInt()-1})
  }

  def selectColumns(connDb: Connection, table: Table): List[String] = {
    val labels = names(getColumns(connDb, "select * from %s".format(table.name)))
    //column selection
    println("table: %s".format(table.name))
    selectSet("column", labels).toList
  }

  def getQuery(connDb: Connection, metadata: DatabaseMetaData, cat: String): Seq[(String,Option[IngestInfo],String)] = {
    if (readLine("Do you have a SQL query to select the data? (y/N)").toLowerCase == "y") {
      List((readLine("table/object name: "),None,readLine("Query=")))
    } else createQueries(connDb, metadata, cat, selectedTables(findTables(metadata, cat, readTableName())), readLine("Denormalize related tables? (y/n)").toLowerCase == "y")
  }

  def createQueries(conn:Connection, metadata: DatabaseMetaData, cat: String, selected: Seq[Table],denormalize: => Boolean): Seq[(String,Option[IngestInfo],String)] = {
    selected.map( table =>{

      val allRelationships = relationships( conn, metadata, cat,table).toSeq
      val relations= selectSet("relation",allRelationships).toList

      val tblDesc=buildIngestInfo(table, conn, relations)
      (table.name,Some(tblDesc), buildQuery(tblDesc))
    })
  }


  def buildIngestInfo(table: Table, conn: Connection, relations: List[Join]): ImportJdbc.IngestInfo = {
    IngestInfo(ImportTable(table.name, selectColumns(conn, table), Left(table)) ::
      relations.map(r => ImportTable(r.refKey.table.name, selectColumns(conn, r.refKey.table), Right(r))))
  }

  def relationships(conn: Connection, metadata: DatabaseMetaData, cat: String, table:Table): Set[Join] = {
    val declaredRelationships=getDeclaredRelationships(metadata,cat,table)
    println("Declared relationsips found: ")
    println(declaredRelationships.mkString(","))
    if (readLine("Infer non-declared relationships? (y/n)").toLowerCase == "y") {
      val idPattern = readLine("Id pattern? (e.g. '%%id' ) ")
      val inferredRelationships= getInferredRelationships(conn, metadata, cat, table, idPattern)
      show(table,inferredRelationships)
      declaredRelationships union (inferredRelationships)
    } else {
      declaredRelationships
    }

  }

  //case class ImportTable(name:String, columns:Seq[String], baseOrJoin:Either[Table,Join]){ val isCollection = baseOrJoin.right.toOption.map(_.exported).getOrElse(false) }
  //case class IngestInfo(tables:Seq[ImportTable])


  def selectedTables(tablesList: Array[Table]): Seq[Table] = {
    selectSet("table", tablesList)
  }

  @tailrec
  private def selectSet[T](label:String, available: Seq[T], selected: Seq[T]=List())(implicit arg0: ClassManifest[T]): Seq[T] =
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


  def present[T](arr:Seq[T])= (1 to arr.length).zip(arr).map(x=>x._1 +":"+ x._2).mkString(", ")
  def show(baseTable:Table,set: Set[Join])= set.map( r=> " %s with %s on %s=%s".format(baseTable.name, r.refKey.table, r.baseColName,r.refKey.columnName )).mkString(", ")

  def readTableName()= {
    val tableName = readLine("Enter table name (blank to show all tables, %% for wildcard )")
    if (tableName == "") null else tableName
  }

  object ParseInt{
    def unapply(s : String) : Option[Int] = try {
      Some(s.toInt)
    } catch {
      case _ : java.lang.NumberFormatException => None
    }
  }
}


