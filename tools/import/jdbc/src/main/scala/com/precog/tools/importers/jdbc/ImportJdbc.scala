package com.precog.tools.importers.jdbc

import java.sql._
import blueeyes.json._
import blueeyes.core.data.DefaultBijections._
import blueeyes.bkka.AkkaDefaults.defaultFutureDispatch
import scala.Some
import blueeyes.core.service.engines.HttpClientXLightWeb
import Datatypes._
import blueeyes.bkka.FutureMonad
import scalaz.{Monad, StreamT,Hoist, ~>}
import akka.dispatch.ExecutionContext
import java.nio.ByteBuffer
import blueeyes.core.http.HttpResponse
import blueeyes.core.data.ByteChunk
import akka.dispatch.Future
import com.precog.tools.importers.common.Ingest._
import DbAccess._
import scalaz.effect.IO
import org.slf4j.LoggerFactory

/**
 * User: gabriel
 * Date: 11/20/12
 */
object ImportJdbc {

  val httpClient=new HttpClientXLightWeb()(defaultFutureDispatch)

  private lazy val logger = LoggerFactory.getLogger("com.precog.tools.importers.jdbc.ImportJdbc")

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

  def buildField( nm: (String,String)) =Option(nm._2).map( s=>JField(nm._1,JString(s)))

  type StrJVMap= Map[String,JValue]

  def objFields( map:StrJVMap, s:Seq[String], tblDesc: ImportTable ):(Option[(String,JValue)],Seq[String])={
    val (tblColValues,rest)=s.splitAt(tblDesc.columns.length)
    val objValues =(tblDesc.columns.zip(tblColValues)).flatMap(buildField(_) )
    val tblName = tblDesc.name.toUpperCase
    val keyValue=
      if (objValues.isEmpty) if (tblDesc.isCollection) Some(tblName->JArray.empty) else None
      else {
        val data=JObject(objValues:_*)
        val obj= if (tblDesc.isCollection) JArray(getElements(map.get(tblName)):+data ) else data
        Some(tblName->obj)
      }
    (keyValue,rest)
  }
  def mkJson[M[+_]](baseName:String, ingestInfo:IngestInfo, row: Seq[String], outStream:StreamT[M,JValue], currentObj:StrJVMap=Map())(implicit M:Monad[M]):(StreamT[M,JValue],StrJVMap) ={
    val baseNameUC=baseName.toUpperCase
    val singleObjMap=buildJsonObjMap(ingestInfo, Map(),row)
    if (currentObj.isEmpty || singleObjMap.get(baseNameUC) == currentObj.get(baseNameUC)){
      val objM=buildJsonObjMap(ingestInfo, currentObj, row)
      (outStream, objM)
    } else {
      val newObj= buildJObject(baseNameUC, currentObj)
      (newObj::outStream,singleObjMap)
    }
  }


  private def buildJObject(baseNameUC: String, currentObj: StrJVMap): JObject = {
    val base = (currentObj(baseNameUC)) --> classOf[JObject]
    val values = (currentObj - baseNameUC)
    val newObj = JObject(base.fields ++ values)
    newObj
  }

  def buildJsonObjMap(ingestInfo: ImportJdbc.IngestInfo, prevMap: ImportJdbc.StrJVMap, s: Seq[String]): StrJVMap = {
    ingestInfo.tables.foldLeft((prevMap, s))({
      case ((m,seq), v) => {
        val (opt, r): (Option[(String, JValue)], Seq[String]) = objFields(m, seq, v) //build a json object from the seq values
        opt.map(kv => (m + kv, r)).getOrElse((m, r))
      }})._1
  }

  def buildBody(data: StreamT[IO,Seq[String]], baseTable: String, i: IngestInfo)(implicit executor: ExecutionContext, m:FutureMonad, io:Monad[IO]): Future[StreamT[Future,JValue]] ={
    Future(data.foldLeft((StreamT.empty[Future,JValue], Map():StrJVMap))(
    { case ((os,currentMap),row)=>mkJson(baseTable,i,row,os,currentMap) }
    ).map( { case (strm,obj)=>
      buildJObject(baseTable.toUpperCase,obj)::strm
    } ).unsafePerformIO())
  }

  def names(cs:Seq[Column])=cs.map(_.name)

  def buildQuery(tblsDesc:IngestInfo) = {
    val columnNames= tblsDesc.tables.flatMap( t=>t.columns.map( c=> "%s.%s".format(t.name, c)) )
    val join=buildJoins(tblsDesc)
    val colSelect=columnNames.mkString(", ")
    val sort=colSelect // for now, the selected columns will suffice, otherwise use buildSort(ingestInfo).mkString(", ")
    "select %s from %s order by %s".format(colSelect,join,sort)
  }

  def executeQuery(connDb: Connection, query: String ): (StreamT[IO,IndexedSeq[String]],IndexedSeq[Column]) = {
    val stmt = connDb.prepareStatement(query)
    val columns = getColumns(stmt)
    val rs = stmt.executeQuery()
    (rsStreamT(rs)(row => for (i <- 1 to columns.size) yield row.getString(i)),columns)
  }

  def getConnection(dbUrl: String, user: String, password: String, database:Option[String]): Connection = {
    val uri= database.map( dbName=>if (dbUrl.endsWith(dbName)) dbUrl else "%s%s".format(dbUrl,dbName)).getOrElse(dbUrl)
    DriverManager.getConnection(uri, user, password)
  }

  def ingest(connDb: Connection, objName: String, query: String, oTblDesc:Option[IngestInfo], ingestPath: =>String, host: =>String, apiKey: =>String)(implicit executor: ExecutionContext):Future[HttpResponse[ByteChunk]] = {
    implicit val M = new FutureMonad(executor)
    val (data,columns) = executeQuery(connDb, query)
    val tblDesc= oTblDesc.getOrElse(IngestInfo(Seq(ImportTable(objName,names(columns),Left(Table("base"))))))

    val path= "%s/%s".format(ingestPath,objName)
    val dataStream:Future[StreamT[Future,ByteBuffer]]= buildBody(data, objName, tblDesc).map(toByteStream(_))
    dataStream.flatMap(sendToPrecog(host,path,apiKey,_))
  }

}





