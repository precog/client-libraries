package com.precog.tools.importers.jdbc


import akka.dispatch.Future
import blueeyes.BlueEyesServiceBuilder
import blueeyes.core.http.{HttpRequest, HttpResponse, HttpStatus}
import blueeyes.core.http.HttpStatusCodes._
import blueeyes.core.data.{ByteChunk, BijectionsChunkString, BijectionsChunkJson}
import blueeyes.json.JsonParser._
import DbAccess._
import DbAnalysis._
import ImportJdbc._
import blueeyes.core.http.HttpHeaders.Connection


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
                  val table = request.parameters('table)
                  val infer = request.parameters.get('infer).map(_.toLowerCase == "y" ).getOrElse(false)
                  val sample = request.parameters.get('sample).map( _.toLowerCase == "y").getOrElse(false)
                  val idPattern=request.parameters.get('pattern).getOrElse("%id")
                  Future {
                    val columns= getColumns(conn,Table(table))
                    val metadata = conn.getMetaData
                    val inferred= if (infer) getInferredRelationships(conn,metadata,cat,Table(table),idPattern,sample) else Set()
                    val references= relationshipDesc(metadata.getExportedKeys(cat, null, table)).toSet ++ inferred
                    HttpResponse[ByteChunk](content = Option(parse("{ columns: [%s] references: [%s] }".format(columns.mkString(","),references.mkString(",")))))
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
                  Future {
                    val metadata = conn.getMetaData
                    val relations:Set[Join]=if (denormalize) {
                      val inferred= if (infer) getInferredRelationships(conn,metadata,cat,table,idPattern,sample) else Set()
                      (getDeclaredRelationships(metadata,cat,table) ++ inferred)
                    } else Set()
                    //case class ImportTable(name:String, columns:Seq[String], baseOrJoin:Either[Table,Join]){ val isCollection = baseOrJoin.right.toOption.map(_.exported).getOrElse(false) }
                    //case class IngestInfo(tables:Seq[ImportTable])

                    //def buildQuery(tblsDesc:IngestInfo) = {
                    val ingestInfo=IngestInfo(ImportTable(table.name, names(getColumns(conn, table)), Left(table)) ::
                      relations.map(r => ImportTable(r.refKey.table.name, names(getColumns(conn, r.refKey.table)), Right(r))).toList)
                    val query=buildQuery(ingestInfo)

                    //def ingest(connDb: Connection, objName:String, query: String, oTblDesc:Option[IngestInfo], basePath: String, ingestPath: String, host: String, apiKey: String) = {
                    val response=ingest(conn,table.name, query, Some(ingestInfo), path, host, apiKey)
                    HttpResponse[ByteChunk](content = Option(new ByteChunk(Array()) ))//JValueToChunk(response) ))
                  }
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