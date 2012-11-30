package com.precog.tools.importers.jdbc

import java.sql._
import blueeyes.json.JsonAST._
import blueeyes.core.data.BijectionsChunkJson.JValueToChunk
import scala.Some
import blueeyes.core.service.engines.HttpClientXLightWeb

/**
 * User: gabriel
 * Date: 11/20/12
 */
object ImportJdbc {

  import DbAccess._

  def buildUnion(tbaseName:String, r: Join,tblName:String): String = {
    val endTable=r.refKey.table.name
    " left join %s %s on %s.%s=%s.%s".format(endTable,tblName,tbaseName, r.baseColName,tblName,r.refKey.columnName)
  }

  def buildJoins(table:Table,relations: Seq[Join],tblNames:Seq[String]) ={
    val baseTblName=tblNames.head
    val joinBase = "%s %s".format(table,baseTblName)
    relations.zip(tblNames.tail).foldLeft(joinBase)( (q,r)=> q + buildUnion(baseTblName,r._1,r._2) )
  }

  def buildSort(tblDesc:TableDesc) =tblDesc.flatMap(
    (tce) => {
      val (tname,cols,_) =tce
      cols.map("%s.%s".format(tname,_))
    }
  )


  def mkPartialJson(baseName:String, tblDesc:TableDesc, s: Seq[String], prevMap:Map[String,JValue]=Map())= {

    def getElements(o:Option[JValue]):List[JValue]= o match {
      case Some(l:JArray) => l.elements
      case _ => Nil
    }
    def toJObject(o:JValue):JObject= o match {
      case j:JObject => j
      case _ => sys.error("base value is not jobject!")
    }

    def buildJValues( ms:(Map[String,JValue],Seq[String]), tblDesc: (String,Seq[String], Boolean) ):(Option[(String,JValue)],Seq[String])={
      val (m,s)=ms
      val (tblName,cols,multiple) = tblDesc
      val (tblColValues,rest)=s.splitAt(cols.length)
      val objValues =(cols.zip(tblColValues)).flatMap(buildField(_) ).toList
      val keyValue=if (objValues.isEmpty) if (multiple) Some(tblName->JArray.empty) else None
      else {
        val data=JObject(objValues)
        val obj= if (multiple) JArray(data:: getElements(m.get(tblName)) ) else data
        Some(tblName->obj)
      }
      (keyValue,rest)
    }

    def buildField( nm: (String,String)) =Option(nm._2).map( s=>JField(nm._1,JString(s)))


    val jsonMap:Map[String,JValue]=tblDesc.foldLeft( (prevMap,s) )(
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

  type TableDesc = Seq[(String,Seq[String],Boolean)]

  def buildQuery(base:Table, tblDesc:TableDesc, relations:Seq[Join]) = {
    val columnNames= tblDesc.flatMap(ncols =>{
      val (name,cols, _) = ncols
      cols.map( c=> "%s.%s".format(name, c))
    } )
    val tblNames = tblDesc.map(_._1)
    val join=buildJoins(base,relations,tblNames)
    val colSelect=columnNames.mkString(", ")
    val sort=colSelect // for now, the selected columns will suffice, otherwise use buildSort(tblDesc).mkString(", ")
    "select %s from %s order by %s".format(colSelect,join,sort)
  }

  def executeQuery(connDb: Connection, query: String ): (Iterator[IndexedSeq[String]],IndexedSeq[String]) = {
    val stmt = connDb.prepareStatement(query)
    val columns = getColumns(stmt)
    val rs = stmt.executeQuery()
    (rsIterator(rs)(row => for (i <- 1 to columns.size) yield row.getString(i)),columns)
  }

  def getConnection(dbUrl: String, user: String, password: String): Connection = {
    DriverManager.getConnection(dbUrl, user, password)
  }

  def ingest(connDb: Connection, objName:String, query: String, oTblDesc:Option[TableDesc], basePath: String, ingestPath: String, host: String, apiKey: String) = {
    println(query)
    val (data,columns) = executeQuery(connDb, query)
    val tblDesc= oTblDesc.getOrElse(Seq((objName,columns.toSeq,false)))
    val path = "%s/%s".format(basePath, ingestPath)
    //def ingest(host: String, path: String, apiKey: String, baseTable:String, tblDesc: TableDesc, data: Iterator[IndexedSeq[String]]) = {
    val body = buildBody(data, objName, tblDesc)
    val fullPath = "%s/ingest/v1/sync/fs%s/".format(host, path)
    println(fullPath)
    val httpClient=new HttpClientXLightWeb()
    httpClient.parameters('apiKey -> apiKey).post(fullPath)(JValueToChunk(body)).onComplete{case r =>println(r)}
  }


  def buildBody(data: Iterator[IndexedSeq[String]], baseTable: String, tblDesc: ImportJdbc.TableDesc): JArray =
    JArray(data.foldLeft((List[JValue](), Map[String, JValue]()))((lm, r) => {
      val (l, m) = lm
      val (values, map) = mkPartialJson(baseTable, tblDesc, r, m)
      (values :: l, map)
    })._1)
}





