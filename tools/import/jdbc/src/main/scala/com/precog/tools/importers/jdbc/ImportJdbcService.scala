package com.precog.tools.importers.jdbc


import akka.dispatch.Future
import blueeyes.BlueEyesServiceBuilder
import blueeyes.core.http.{HttpRequest, HttpResponse, HttpStatus}
import blueeyes.core.http.HttpStatusCodes._
import blueeyes.core.data.DefaultBijections._
import blueeyes.core.data.ByteChunk
import DbAccess._
import DbAnalysis._
import ImportJdbc._
import JsonImplicits._
import java.sql.{DatabaseMetaData, Connection}
import blueeyes.json.{JValue, JString, JArray}
import Datatypes._


/**
 * User: gabriel
 * Date: 11/19/12
 */
trait ImportJdbcService extends BlueEyesServiceBuilder {


  val host="https://beta.precog.com" // "https://devapi.precog.com" //TODO move to trait

  def handleRequest[T](f: HttpRequest[T]=> Future[HttpResponse[T]])=
    (request: HttpRequest[T]) =>
      try {
        f(request)
      } catch {
        case e:Exception => Future { HttpResponse[T](status = HttpStatus(BadRequest)) }
      }

  def withConnectionFromRequest[T](r:HttpRequest[T])(f: (Connection,HttpRequest[T])=> Future[HttpResponse[T]])= {
    val dbUrl = r.parameters('dbUrl)
    val database= r.parameters.get('database)
    val user = r.parameters.get('user).getOrElse(null)
    val pwd = r.parameters.get('password).getOrElse(null)
    val c=getConnection(dbUrl, user, pwd,database)
    try {
      f(c,r)
    } finally {
      c.close()
    }
  }

  def handleRequestWithConnection[T](f: (Connection,HttpRequest[T])=> Future[HttpResponse[T]])= handleRequest( (r: HttpRequest[T]) =>  withConnectionFromRequest(r)(f))

  def optionYes(ob:Option[String])=ob.map(_.toLowerCase == "y").getOrElse(false)
  /*def response[T](f: HttpRequest[T] => HttpResponse[T] )(request: HttpRequest[T]):Future[HttpResponse[T]] = {


  }*/

  def getJoins(infer: Boolean, conn: Connection, metadata: DatabaseMetaData, cat: Option[String], table: Table, idPattern: String, sample: Boolean): Set[Join] = {
      val inferred = if (infer) getInferredRelationships(conn, metadata, cat, table, idPattern, sample) else Set()
      (getDeclaredRelationships(metadata, cat, table) ++ inferred)
  }

  def buildIngestInfo(conn: Connection, table: Table, relations: Set[Join]): IngestInfo = {
    IngestInfo(ImportTable(table.name, names(getColumns(conn, table)), Left(table)) ::
      relations.map(r => ImportTable(r.refKey.table.name, names(getColumns(conn, r.refKey.table)), Right(r))).toList)
  }




  val importService = service("JdbcImportService", "1.0.0") { context =>
    startup {
      Future { () }
    } ->
    request { config: Unit =>
      path("/metadata") {
        path("/databases" ) {
          get {
            handleRequestWithConnection( (conn:Connection,request:HttpRequest[ByteChunk]) =>{
              val tables=JArray(oneColumnRs(conn.getMetaData.getCatalogs).map(JString(_)).toList)
              Future {
                HttpResponse[ByteChunk](content = Option(tables))
              }
            }
            )
          }
        } ~
          //get tables
          path("/databases" / 'database / "tables" ) {
            get {
              handleRequestWithConnection( (conn:Connection,request:HttpRequest[ByteChunk]) => {
                val cat = request.parameters.get('database)
                val ts=findTables(conn.getMetaData,cat,None)
                val result = JArray(ts.map(t=>JString(t.name)).toList)
                Future {
                  HttpResponse[ByteChunk](content = Option(result))
                }
              }
              )
            }
          } ~
          path("/databases" / 'database / "tables" / 'table ) {
            get {
              handleRequestWithConnection( (conn:Connection,request:HttpRequest[ByteChunk]) => {
                val cat = request.parameters.get('database)
                val table = Table(request.parameters('table))
                val infer = optionYes(request.parameters.get('infer))
                val sample = optionYes(request.parameters.get('sample))
                val idPattern=request.parameters.get('pattern).getOrElse("%ID")
                val metadata = conn.getMetaData
                val joins= getJoins(infer,conn,metadata,cat,table,idPattern,sample)
                val ingestInfo=buildIngestInfo(conn, table, joins)
                Future {
                  HttpResponse[ByteChunk](content = Option(jvalueToChunk(ingestInfo)))
                }
              })
            }
          }
      } ~
      path("/ingest/") {
        path('database / "query") {
          post {
            handleRequestWithConnection( (conn:Connection,request:HttpRequest[ByteChunk]) => {
              val objName = request.parameters('objName)
              val query = request.parameters('q)
              val apiKey= request.parameters('apiKey)
              val path= request.parameters('path)
              ingest(conn,objName, query, None,path, host,apiKey)
            })
          }
        } ~
        path('database / "table" / 'table / "auto") {
          post {
            handleRequestWithConnection( (conn:Connection,request:HttpRequest[ByteChunk]) => {
              val cat = request.parameters.get('database)
              val apiKey= request.parameters('apiKey)
              val path= request.parameters('path)
              val table= Table(request.parameters('table))
              val denormalize = request.parameters('denormalize) == "y"
              val infer = optionYes(request.parameters.get('infer))
              val sample = optionYes(request.parameters.get('sample))
              val idPattern=request.parameters.get('pattern).getOrElse("%ID")
              val metadata = conn.getMetaData
              val relations:Set[Join]= if (denormalize) getJoins(infer, conn, metadata, cat, table, idPattern, sample) else Set()
              val ingestInfo=buildIngestInfo(conn, table, relations)
              val query=buildQuery(ingestInfo)
              ingest(conn,table.name, query, Some(ingestInfo), path, host, apiKey)
            })
          }
        }~
          path('database / "table" / 'table / "config") {
            post {
              handleRequest( (request:HttpRequest[ByteChunk]) => {
                val apiKey= request.parameters('apiKey)
                val path= request.parameters('path)
                val table= Table(request.parameters('table))
                val cToJ=chunkToFutureJValue
                request.content.map(cToJ(_)).map(_.flatMap( ingestInfo =>{
                  withConnectionFromRequest(request)( (conn:Connection,_)=>{
                  val query = buildQuery(ingestInfo)
                  ingest(conn,table.name, query, Some(ingestInfo), path, host, apiKey)
                  })
                })).getOrElse(Future{ HttpResponse[ByteChunk](content = None) })
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