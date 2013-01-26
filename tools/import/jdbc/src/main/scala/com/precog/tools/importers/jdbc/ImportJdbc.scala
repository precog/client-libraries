package com.precog.tools.importers.jdbc

import java.sql._
import blueeyes.json._
import blueeyes.core.data.DefaultBijections._
import blueeyes.core.service._
import blueeyes.bkka.AkkaDefaults.defaultFutureDispatch
import scala.Some
import blueeyes.core.service.engines.HttpClientXLightWeb
import Datatypes._
import blueeyes.bkka.FutureMonad
import scalaz.{Hoist, StreamT, ~>}
import akka.dispatch.{Await, ExecutionContext, Future}
import java.nio.ByteBuffer
import scalaz.Id._
import annotation.tailrec
import akka.util.Duration
import blueeyes.core.http.HttpResponse
import blueeyes.core.data.ByteChunk
import akka.dispatch.Future


/**
 * User: gabriel
 * Date: 11/20/12
 */
object ImportJdbc {

  import DbAccess._

  val httpClient=new HttpClientXLightWeb()(defaultFutureDispatch)

  case class ImportTable(name:String, columns:Seq[String], baseOrJoin:Either[Table,Join]){ val isCollection = baseOrJoin.right.toOption.map(_.exported).getOrElse(false) }
  case class IngestInfo(tables:Seq[ImportTable])

  def buildUnion(tbaseName:String, t:ImportTable): Option[String]=
    t.baseOrJoin.right.toOption.map( j =>
    " left join %s %s on %s.%s=%s.%s".format(j.refKey.table.name,t.name,tbaseName, j.baseColName,t.name,j.refKey.columnName)
  )

  def buildJoins(tblDesc:IngestInfo)={
    val baseTblDesc=tblDesc.tables.head
    val joinBase = "%s %s".format(tblDesc.tables.head.baseOrJoin.left.get,baseTblDesc.name)
    val joins = tblDesc.tables.tail.flatMap( t=> buildUnion(baseTblDesc.name,t) )
    "%s%s".format(joinBase,joins.mkString )
  }

  def buildSort(ingestInfo:IngestInfo) =ingestInfo.tables.flatMap( t => t.columns.map("%s.%s".format(t.name,_)) )

  def getElements(o:Option[JValue]):List[JValue]= o match {
    case Some(l:JArray) => l.elements
    case _ => Nil
  }

  def toJObject(o:JValue):JObject= o match {
    case j:JObject => j
    case _ => sys.error("base value is not jobject!")
  }

  def buildField( nm: (String,String)) =Option(nm._2).map( s=>JField(nm._1,JString(s)))

  type StrJVMap= Map[String,JValue]

  def buildJValues( map:StrJVMap, s:Seq[String], tblDesc: ImportTable ):(Option[(String,JValue)],Seq[String])={
    val (tblColValues,rest)=s.splitAt(tblDesc.columns.length)
    val objValues =(tblDesc.columns.zip(tblColValues)).flatMap(buildField(_) ).toList
    val tblName = tblDesc.name.toUpperCase
    val keyValue=
      if (objValues.isEmpty) if (tblDesc.isCollection) Some(tblName->JArray.empty) else None
      else {
        val data=JObject(objValues)
        val obj= if (tblDesc.isCollection) JArray(getElements(map.get(tblName)):+data ) else data
        Some(tblName->obj)
      }
    (keyValue,rest)
  }

  @tailrec
  def mkPartialJson(baseName:String, ingestInfo:IngestInfo, dataStream: StreamT[Id,Seq[String]], prevMap:StrJVMap=Map()):Option[(JValue,StreamT[Id,Seq[String]])] =
    if (dataStream.isEmpty) None
    else {
      val s=dataStream.head
      val tail=dataStream.tail
      val jsonMap=buildJsonObjMap(ingestInfo, prevMap, s)
      val baseNameUC=baseName.toUpperCase
      //peek into the stream
      val nextJsonMap:StrJVMap=if (tail.isEmpty) Map() else buildJsonObjMap(ingestInfo, Map(), tail.head)
      if ( !nextJsonMap.isEmpty && (jsonMap.get(baseNameUC) == nextJsonMap.get(baseNameUC)) ) {
        //if next row is the same object, keep building
        mkPartialJson(baseNameUC,ingestInfo,tail,jsonMap)
      } else {
        val base= toJObject(jsonMap(baseNameUC))
        val values = (jsonMap-baseNameUC).map(nv => JField(nv._1, nv._2)).toList
        Some(JObject(base.fields ++ values),tail)
      }
    }


  def buildJsonObjMap(ingestInfo: ImportJdbc.IngestInfo, prevMap: ImportJdbc.StrJVMap, s: Seq[String]): StrJVMap = {
    ingestInfo.tables.foldLeft((prevMap, s))(
      (ms, v) => {
        val (m,seq)=ms
        val (opt, r): (Option[(String, JValue)], Seq[String]) = buildJValues(m, seq, v) //build a json object from the seq values
        opt.map(kv => (m + kv, r)).getOrElse((m, r))
      })._1
  }

  def names(cs:Seq[Column])=cs.map(_.name)

  def buildQuery(tblsDesc:IngestInfo) = {
    val columnNames= tblsDesc.tables.flatMap( t=>t.columns.map( c=> "%s.%s".format(t.name, c)) )
    val join=buildJoins(tblsDesc)
    val colSelect=columnNames.mkString(", ")
    val sort=colSelect // for now, the selected columns will suffice, otherwise use buildSort(ingestInfo).mkString(", ")
    "select %s from %s order by %s".format(colSelect,join,sort)
  }

  def executeQuery(connDb: Connection, query: String ): (StreamT[Id,IndexedSeq[String]],IndexedSeq[Column]) = {
    val stmt = connDb.prepareStatement(query)
    val columns = getColumns(stmt)
    val rs = stmt.executeQuery()
    (rsStreamT(rs)(row => for (i <- 1 to columns.size) yield row.getString(i)),columns)
  }

  def getConnection(dbUrl: String, user: String, password: String, database:Option[String]): Connection = {
    val uri= database.map( dbName=>if (dbUrl.endsWith(dbName)) dbUrl else "%s%s".format(dbUrl,dbName)).getOrElse(dbUrl)
    DriverManager.getConnection(uri, user, password)
  }

  def ingest(connDb: Connection, objName:String, query: String, oTblDesc:Option[IngestInfo], ingestPath: =>String, host: =>String, apiKey: =>String)(implicit executor: ExecutionContext):Future[HttpResponse[ByteChunk]] = {
    implicit val M = new FutureMonad(executor)
    val (data,columns) = executeQuery(connDb, query)
    val tblDesc= oTblDesc.getOrElse(IngestInfo(Seq(ImportTable(objName,names(columns),Left(Table("base"))))))

    val dataStream:StreamT[Future,ByteBuffer] =buildBody(data, objName, tblDesc)
      .map(jv=>ByteBuffer.wrap({val js="%s\n".format(jv.renderCompact); print(js); js}.getBytes("UTF-8")))

    val body:ByteChunk= Right(dataStream)
    val fullPath = "%s/ingest/v1/sync/fs%s/%s".format(host, ingestPath,objName)
    //TODO add owner account id
    println("sending to ingest: path=%s query=%s".format(fullPath,query))
    httpClient.parameters('apiKey -> apiKey).post(fullPath)(body)
  }

  def buildBody(data: StreamT[Id,Seq[String]], baseTable: String, i: IngestInfo)(implicit executor: ExecutionContext, m:FutureMonad): StreamT[Future,JValue] =
    StreamT.unfoldM[Future,JValue,StreamT[Id,Seq[String]]](data)(ds=>
        if (ds.isEmpty) Future(None)
        else Future(mkPartialJson(baseTable,i,ds)))


}





