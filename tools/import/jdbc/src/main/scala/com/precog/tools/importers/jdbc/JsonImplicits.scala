package com.precog.tools.importers.jdbc

import blueeyes.json._

import com.precog.tools.importers.jdbc.ImportJdbc.{IngestInfo, ImportTable}
import Datatypes._

/**
 * User: gabriel
 * Date: 12/3/12
 */
object JsonImplicits {



  implicit def table2Json(t:Table) = JString(t.name)
  implicit def json2Table(j:JString) = Table(j.value)

  def string(j:JValue)= j match {
    case JString(s) => s
    case _ => sys.error("invalid string coversion")
  }

  implicit def key2Json(k:Key):JValue=JObject(List(JField("table",k.table),JField("columnName",JString(k.columnName))))
  implicit def json2Key(j:JValue):Key = Key(Table(string(j \\ "table" )), string (j \\ "columnName"))

  implicit def join2Json(j:Join):JValue=JObject(List(JField("baseColName",JString(j.baseColName)),JField("refKey", j.refKey),JField("exported",JBool(j.exported))))
  implicit def Json2Join(j:JValue):Join=(for {
    JObject(join) <- j
    JField("baseColName",JString(baseColName)) <- join
    JField("refKey", refKey) <-join
    JField("exported",JBool(exprt)) <- join
  } yield Join(baseColName,refKey,exprt)).head

  implicit def importTable2Json(i:ImportTable):JValue= JObject(List(
    JField("name",JString(i.name)),
    JField("columns",JArray(i.columns.map(JString(_)).toList)),
    i.baseOrJoin.fold(t=> JField("base",JString(t.name)), j => JField("join", j)))
  )

  implicit def json2ImportTable(j:JValue):ImportTable= (for {
    JString(name) <- j \? "name"
    JArray(columns) <-j \? "columns"
  } yield {
    (j \? "join").map(joins=>ImportTable(name,columns.map(string(_)), Right(joins))).getOrElse(
    (j \? "base").map(base=>ImportTable(name,columns.map(string(_)), Left(Table(string(base))))).get)
  }).get



  implicit def ingestInfo2Json(ii:IngestInfo):JValue=JArray(ii.tables.map(importTable2Json(_)).toList)
  implicit def json2IngestInfo(j:JValue):IngestInfo=j match {
    case JArray(vals) => IngestInfo(vals.map(json2ImportTable(_)))
    case _ => sys.error("wrong implicit to IngestInfo")
  }
}
