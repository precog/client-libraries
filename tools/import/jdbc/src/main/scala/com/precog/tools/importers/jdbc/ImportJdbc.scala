package com.precog.tools.importers.jdbc

import java.sql._
import blueeyes.json.JsonAST._
import blueeyes.core.data.BijectionsChunkJson.{JValueToChunk,ChunkToJValue}
import scala.Some
import blueeyes.core.service.engines.HttpClientXLightWeb

/**
 * User: gabriel
 * Date: 11/20/12
 */
object ImportJdbc {

  import DbAccess._

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


  def mkPartialJson(baseName:String, ingestInfo:IngestInfo, s: Seq[String], prevMap:Map[String,JValue]=Map())= {

    def getElements(o:Option[JValue]):List[JValue]= o match {
      case Some(l:JArray) => l.elements
      case _ => Nil
    }
    def toJObject(o:JValue):JObject= o match {
      case j:JObject => j
      case _ => sys.error("base value is not jobject!")
    }

    def buildJValues( ms:(Map[String,JValue],Seq[String]), tblDesc: ImportTable ):(Option[(String,JValue)],Seq[String])={
      val (m,s)=ms
      val (tblColValues,rest)=s.splitAt(tblDesc.columns.length)
      val objValues =(tblDesc.columns.zip(tblColValues)).flatMap(buildField(_) ).toList
      val tblName = tblDesc.name
      val keyValue=if (objValues.isEmpty) if (tblDesc.isCollection) Some(tblName->JArray.empty) else None
      else {
        val data=JObject(objValues)
        val obj= if (tblDesc.isCollection) JArray(data:: getElements(m.get(tblName)) ) else data
        Some(tblName->obj)
      }
      (keyValue,rest)
    }

    def buildField( nm: (String,String)) =Option(nm._2).map( s=>JField(nm._1,JString(s)))

    val jsonMap:Map[String,JValue]=ingestInfo.tables.foldLeft( (prevMap,s) )(
      (ms,v) =>{
        val (opt,r)= buildJValues(ms,v)
        val (m,_)=ms
        opt.map( (kobj)=>{
          val (k,obj) =kobj
          if (k!=baseName)
            (m+(kobj),r)
          else if (prevMap.isEmpty || prevMap(k)!= obj)
            (Map(kobj),r)
          else (m,r)
        }).getOrElse((m,r))
      } )._1

    val base:JObject = toJObject(jsonMap(baseName))
    val values:List[JField] = (jsonMap-baseName).map(nv => JField(nv._1, nv._2)).toList
    (JObject(base.fields ++ values),jsonMap)
  }


  def names(cs:Seq[Column])=cs.map(_.name)

  def buildQuery(tblsDesc:IngestInfo) = {
    val columnNames= tblsDesc.tables.flatMap( t=>t.columns.map( c=> "%s.%s".format(t.name, c)) )
    val join=buildJoins(tblsDesc)
    val colSelect=columnNames.mkString(", ")
    val sort=colSelect // for now, the selected columns will suffice, otherwise use buildSort(ingestInfo).mkString(", ")
    "select %s from %s order by %s".format(colSelect,join,sort)
  }

  def executeQuery(connDb: Connection, query: String ): (Iterator[IndexedSeq[String]],IndexedSeq[Column]) = {
    val stmt = connDb.prepareStatement(query)
    val columns = getColumns(stmt)
    val rs = stmt.executeQuery()
    (rsIterator(rs)(row => for (i <- 1 to columns.size) yield row.getString(i)),columns)
  }

  def getConnection(dbUrl: String, user: String, password: String): Connection = {
    DriverManager.getConnection(dbUrl, user, password)
  }

  def ingest(connDb: Connection, objName:String, query: String, oTblDesc:Option[IngestInfo], ingestPath: =>String, host: =>String, apiKey: =>String) = {
    println(query)
    val (data,columns) = executeQuery(connDb, query)
    val tblDesc= oTblDesc.getOrElse(IngestInfo(Seq(ImportTable(objName,names(columns),Left(Table("base"))))))

    val body = buildBody(data, objName, tblDesc)
    val fullPath = "%s/ingest/v1/sync/fs%s/".format(host, ingestPath)
    println(fullPath)
    val httpClient=new HttpClientXLightWeb()
    httpClient.parameters('apiKey -> apiKey).post(fullPath)(JValueToChunk(body)).onSuccess{case r =>(r.content.map(ChunkToJValue(_)))}
  }

  def buildBody(data: Iterator[IndexedSeq[String]], baseTable: String, i: IngestInfo): JArray =
    JArray(data.foldLeft((List[JValue](), Map[String, JValue]()))((lm, r) => {
      val (l, m) = lm
      val (values, map) = mkPartialJson(baseTable, i, r, m)
      (values :: l, map)
    })._1)
}





