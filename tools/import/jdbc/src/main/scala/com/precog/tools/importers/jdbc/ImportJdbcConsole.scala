package com.precog.tools.importers.jdbc

import annotation.tailrec
import java.sql.{Connection, DatabaseMetaData}
import DbAccess._
import DbAnalysis._
import ImportJdbc._
import blueeyes.bkka.AkkaDefaults._
import blueeyes.core.http.HttpResponse
import blueeyes.core.data._
import scala.Left
import com.precog.tools.importers.jdbc.Datatypes.Join
import com.precog.tools.importers.jdbc.ImportJdbc.IngestInfo
import scala.Some
import scala.Right
import com.precog.tools.importers.jdbc.ImportJdbc.ImportTable
import com.precog.tools.importers.jdbc.Datatypes.Table
import akka.dispatch.{Future, Await}
import akka.util.Duration

/**
 * User: gabriel
 * Date: 11/16/12
 */
object ImportJdbcConsole {

  implicit val as=actorSystem

  def main(args:Array[String]){
    println("Welcome to Precog JDBC import wizard")
    lazy val dbUrl=readLine("Enter database URL:")
    lazy val user=readLine("User:")
    lazy val password = readLine("Password:")
    // use api key and dispatch to call ingest
    lazy val host=readLine("Precog ingestion host")
    lazy val apiKey=readLine("API KEY for ingestion")
    lazy val basePath=readLine("Base ingestion path ( /{userId}/....)")

    val fresult=importJdbc(dbUrl,user,password, host, apiKey, basePath)

    Await.result(Future.sequence(fresult),Duration("24 hours")).map(
      result => result match {
        case HttpResponse(_ ,_,Some(Left(buffer)),_) => { println(new String(buffer.array(), "UTF-8"))}
        case _ => "error %s".format(result.toString())
      }
    )
    as.shutdown()
  }

  def importJdbc(dbUrl: =>String, user: =>String, password: =>String, host: =>String, apiKey: =>String, basePath: =>String)={

    val catConn= getConnection(dbUrl, user, password,None)
    val cat= getCatalogs(catConn.getMetaData)

    //for querying tables, the connection must be specific to a database
    val connDb= getConnection(dbUrl, user, password,Some(cat))
    val tqs = getQuery(connDb, cat)

    tqs.map( tqs => {
      val (table,tDesc,q) = tqs
      val path= "%s/%s".format(basePath, table)
      println("importing %s".format(table))
      ingest(connDb,table, q, tDesc, path, host, apiKey).onComplete {
        case Right(result) => callSucceded(result)
        case Left(failure) => println("Failed to import %s, error: %s".format(table,failure.getMessage))
      }
    })
  }

  def callSucceded(response:HttpResponse[ByteChunk]){
    response match {
      case HttpResponse(_ ,_,Some(Left(buffer)),_) => println("Result: %s".format(new String(buffer.array(), "UTF-8")))
      case _ => println("Unexpected stream in %s".format(response))
    }
   }

  def getCatalogs(metadata: DatabaseMetaData): String = {
    println("Catalogs:")
    val catalogs = oneColumnRs(metadata.getCatalogs).toArray
    selectOne("Catalog/Database",catalogs).getOrElse("")
  }

  def selectColumns(connDb: Connection, table: Table): List[String] = {
    val labels = names(getColumns(connDb, "select * from %s".format(table.name)))
    //column selection
    println("Table: %s".format(table.name.toUpperCase))
    selectSet("column", labels).toList
  }

  def getQuery(connDb: Connection, cat: String): Seq[(String,Option[IngestInfo],String)] = {
    if (readLine("Do you have a SQL query to select the data? (y/N)").toLowerCase == "y") {
      List((readLine("table/object name: "),None,readLine("Query=")))
    } else {
      val tblName=readTableName()
      val metadata= connDb.getMetaData
      val tables=findTables(metadata, Some(cat), tblName)
      createQueries(connDb, metadata, cat, selectedTables(tables), readLine("Denormalize related tables? (y/n)").toLowerCase == "y")
    }
  }

  def createQueries(conn:Connection, metadata: DatabaseMetaData, cat: String, selected: Seq[Table],denormalize: => Boolean): Seq[(String,Option[IngestInfo],String)] = {
    selected.map( table =>{

      val allRelationships = relationships( conn, metadata, None,table).toSeq
      println(allRelationships)
      val relations= selectSet("relation",allRelationships).toList

      val tblDesc=buildIngestInfo(table, conn, relations)
      (table.name,Some(tblDesc), buildQuery(tblDesc))
    })
  }


  def buildIngestInfo(table: Table, conn: Connection, relations: List[Join]): ImportJdbc.IngestInfo = {
    IngestInfo(ImportTable(table.name, selectColumns(conn, table), Left(table)) ::
      relations.map(r => ImportTable(r.refKey.table.name, selectColumns(conn, r.refKey.table), Right(r))))
  }

  def relationships(conn: Connection, metadata: DatabaseMetaData, cat: Option[String], table:Table): Set[Join] = {
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
        case s if (available.exists(_.toString == s)) => {
          val elem:T =availArray.find(_.toString == s).get
          selectSet(label,available.filterNot(_==elem), selected:+elem)
        }
        case _ => selectSet(label,available, selected)
      }
    }

  @tailrec
  private def selectOne[T](label:String, available: Seq[T] )(implicit arg0: ClassManifest[T]): Option[T] =
    if (available.isEmpty) None
    else {
      val availArray=available.toArray

      println("Select a %s:".format(label))
      println(present(availArray))

      println("Select a number/enter the name: ")

      val selIdx = readLine()
      selIdx match {
        case ParseInt(x) if (x<=available.size) => Option(availArray(x - 1))
        case s if (available.exists(_.toString == s)) => availArray.find(_.toString == s)
        case _ => selectOne(label,available)
      }
    }


  def present[T](arr:Seq[T])= (1 to arr.length).zip(arr).map(x=>x._1 +":"+ x._2).mkString(", ")
  def show(baseTable:Table,set: Set[Join])= set.map( r=> " %s with %s on %s=%s".format(baseTable.name, r.refKey.table, r.baseColName,r.refKey.columnName )).mkString(", ")

  def readTableName()= {
    val tableName = readLine("Enter table name (blank to show all tables, %% for wildcard )")
    if (tableName == "") None else Some(tableName)
  }

  object ParseInt{
    def unapply(s : String) : Option[Int] = try {
      Some(s.toInt)
    } catch {
      case _ : java.lang.NumberFormatException => None
    }
  }
}


