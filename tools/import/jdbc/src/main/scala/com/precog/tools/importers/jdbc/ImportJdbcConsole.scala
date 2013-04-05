package com.precog.tools.importers.jdbc

import java.sql.{Connection, DatabaseMetaData}
import DbAccess._
import DbAnalysis._
import ImportJdbc._
import blueeyes.bkka.AkkaDefaults._
import scala.Left
import com.precog.tools.importers.jdbc.Datatypes.Join
import com.precog.tools.importers.jdbc.ImportJdbc.IngestInfo
import scala.Some
import scala.Right
import com.precog.tools.importers.jdbc.ImportJdbc.ImportTable
import com.precog.tools.importers.jdbc.Datatypes.Table
import com.precog.tools.importers.common.ConsoleUtils._
import org.slf4j.LoggerFactory
import com.precog.tools.importers.common.Ingest._

/**
 * User: gabriel
 * Date: 11/16/12
 */
object ImportJdbcConsole {

  private lazy val logger = LoggerFactory.getLogger("com.precog.tools.importers.jdbc.ImportJdbcConsole")

  implicit val as=actorSystem

  Option(System.getProperty("jdbc.driver")).map(driver => Class.forName(driver))

  def main(args:Array[String]){
    println("Welcome to Precog JDBC import wizard")
    lazy val dbUrl=readLine("Enter database URL:")
    lazy val user=readLine("User:")
    lazy val password = readLine("Password:")
    // use api key and dispatch to call ingest
    lazy val host=readLine("Precog ingestion host")
    lazy val apiKey=readLine("API KEY for ingestion")
    lazy val basePath=readLine("Base ingestion path ( /{userId}/....)")

    importJdbc(dbUrl,user,password, host, apiKey, basePath)
    as.shutdown()
  }

  def importJdbc(dbUrl: =>String, user: =>String, password: =>String, host: =>String, apiKey: =>String, basePath: =>String):Unit={

    val catConn= getConnection(dbUrl, user, password,None)
    val cat= getCatalogs(catConn.getMetaData)

    //for querying tables, the connection must be specific to a database
    val connDb= getConnection(dbUrl, user, password,Some(cat))
    val tqs = getQuery(connDb, cat)

    tqs.map( tqs => {
      val (table,tDesc,q) = tqs
      val path= "%s/%s".format(basePath, table)
      logger.info("importing %s".format(table))
      ingest(connDb,table, q, tDesc, path, host, apiKey).onComplete {
        case Right(result) => callSucceded(result)
        case Left(failure) => logger.error("Failed to import %s, error: %s".format(table,failure.getMessage))
      }
    })
  }

  def getCatalogs(metadata: DatabaseMetaData): String = {
    println("Catalogs:")
    val catalogs = oneColumnRs(metadata.getCatalogs)
    selectOne("Catalog/Database",catalogs)
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
      present(allRelationships)
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

  def present[T](arr:Seq[T])= arr.zipWithIndex.map(x=>x._1 +":"+ x._2).mkString(", ")
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


