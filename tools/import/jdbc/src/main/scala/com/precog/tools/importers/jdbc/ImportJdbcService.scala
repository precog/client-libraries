package com.precog.tools.importers.jdbc


import akka.dispatch.Future
import blueeyes.BlueEyesServiceBuilder
import blueeyes.core.http.{HttpRequest, HttpResponse, HttpStatus}
import blueeyes.core.http.HttpStatusCodes._
import blueeyes.core.data.{ByteChunk, BijectionsChunkString, BijectionsChunkJson}
import DbAccess._
import DbAnalysis._
import ImportJdbc._
import JsonImplicits._
import java.sql.{DatabaseMetaData, Connection}


/**
 * User: gabriel
 * Date: 11/19/12
 */
trait ImportJdbcService extends BlueEyesServiceBuilder with BijectionsChunkJson with BijectionsChunkString{


  val host="http://beta.precog.com" //TODO move to trait

  def handleRequest[T](f: HttpRequest[T]=> Future[HttpResponse[T]])=
    (request: HttpRequest[T]) =>
      try {
        f(request)
      } catch {
        case _:Exception => Future { HttpResponse[T](status = HttpStatus(BadRequest)) }
      }

  /*def response[T](f: HttpRequest[T] => HttpResponse[T] )(request: HttpRequest[T]):Future[HttpResponse[T]] = {


  }*/

  def getJoins(infer: Boolean, conn: Connection, metadata: DatabaseMetaData, cat: String, table: Table, idPattern: String, sample: Boolean): Set[Join] = {
      val inferred = if (infer) getInferredRelationships(conn, metadata, cat, table, idPattern, sample) else Set()
      (getDeclaredRelationships(metadata, cat, table) ++ inferred)
  }

  def buildIngestInfo(conn: Connection, table: Table, relations: Set[Join]): IngestInfo = {
    IngestInfo(ImportTable(table.name, names(getColumns(conn, table)), Left(table)) ::
      relations.map(r => ImportTable(r.refKey.table.name, names(getColumns(conn, r.refKey.table)), Right(r))).toList)
  }


  def getConnectionFromRequest(r:HttpRequest[ByteChunk])= {
    val dbUrl = r.parameters('dbUrl)
    val database= r.parameters.get('database).getOrElse("")
    getConnection("%s%s".format(dbUrl,database), r.parameters('user), r.parameters('password))
  }

  val importService = service("JdbcImportService", "1.0.0") {
    context =>
        startup {
          Future { () }
        } ->
        request { config: Unit =>
          path("/metadata") {
            //get databases
            path("/databases" ) {
              get {
                  handleRequest((request: HttpRequest[ByteChunk]) => {
                    val conn= getConnectionFromRequest(request)
                    Future {
                      HttpResponse[ByteChunk](content = Option(oneColumnRs(conn.getMetaData.getCatalogs).toArray.mkString(",")))
                    }
                  })
              }
            } ~
            //get tables
            path("/databases" / 'database / "tables" ) {
              get {
                handleRequest( (request: HttpRequest[ByteChunk]) => {
                    val conn= getConnectionFromRequest(request)
                    val cat = request.parameters('database)
                    Future {
                      HttpResponse[ByteChunk](content = Option(findTables(conn.getMetaData,cat,"").mkString(",")))
                    }
                  }
                )
              }
            } ~
            path("/databases" / 'database / "tables" / 'table ) {
              get {
                handleRequest( (request: HttpRequest[ByteChunk]) => {
                  val conn= getConnectionFromRequest(request)
                  val cat = request.parameters('database)
                  val table = Table(request.parameters('table))
                  val infer = request.parameters.get('infer).map(_.toLowerCase == "y" ).getOrElse(false)
                  val sample = request.parameters.get('sample).map( _.toLowerCase == "y").getOrElse(false)
                  val idPattern=request.parameters.get('pattern).getOrElse("%id")
                  Future {
                    val metadata = conn.getMetaData
                    val joins= getJoins(infer,conn,metadata,cat,table,idPattern,sample)
                    val ingestInfo=buildIngestInfo(conn, table, joins)
                    HttpResponse[ByteChunk](content = Option(JValueToChunk(ingestInfo)))
                  }
                })
              }
            }
          } ~
          path("/ingest") {
            path('database / "query") {
              post {
                handleRequest( (request: HttpRequest[ByteChunk]) => {
                  val conn= getConnectionFromRequest(request)
                  val objName = request.parameters('objName)
                  val query = request.parameters('q)
                  val apiKey= request.parameters('apiKey)
                  val path= request.parameters('path)
                  ingest(conn,objName, query, None,path, host,apiKey)
                })
              }
            } ~
            path('database / "table" / 'table) {
              post {
                handleRequest( (request: HttpRequest[ByteChunk]) => {
                  val conn= getConnectionFromRequest(request)
                  val cat = request.parameters('database)
                  val apiKey= request.parameters('apiKey)
                  val path= request.parameters('path)
                  val table= Table(request.parameters('table))
                  val denormalize = request.parameters('denormalize) == "y"
                  val infer = request.parameters('infer) == "y"
                  val idPattern=request.parameters.get('pattern).getOrElse("%id")
                  val sample = request.parameters('sample) == "y"
                  val metadata = conn.getMetaData
                  val relations:Set[Join]= if (denormalize) getJoins(infer, conn, metadata, cat, table, idPattern, sample) else Set()
                  val ingestInfo=buildIngestInfo(conn, table, relations)
                  val query=buildQuery(ingestInfo)
                  ingest(conn,table.name, query, Some(ingestInfo), path, host, apiKey)
                })
              }
          }
        }
      } ->
      shutdown { config =>
        Future { () }
      }
  }

}