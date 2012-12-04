package com.precog.tools.importers.jdbc

import blueeyes.json.JsonAST._
import blueeyes.json.JsonAST.JString
import blueeyes.json.Implicits._
import blueeyes.json.JsonAST.JField
import com.precog.tools.importers.jdbc.ImportJdbc.{IngestInfo, ImportTable}

/**
 * User: gabriel
 * Date: 12/3/12
 */
object JsonImplicits {

  implicit def key2Json(k:Key):JValue=JObject(List(JField("table",k.table.name),JField("columnName",k.columnName)))
  implicit def json2Key(j:JValue):Key = Key(Table((j \\ "table" toString())),  j \\ "columnName" toString())

  implicit def join2Json(j:Join):JValue=JObject(List(JField("baseColName",j.baseColName),JField("refKey", j.refKey),JField("exported",JBool(j.exported))))
  implicit def Json2Join(j:JValue):Join= j match {
    case JObject(List(JField("baseColName",JString(baseColName)),JField("refKey", refKey),JField("exported",JBool(exported)))) =>
      Join(baseColName,refKey,exported)
    case _ => sys.error("wrong implicit to Join")
  }

  implicit def importTable2Json(i:ImportTable):JValue= JObject(List(
    JField("name",i.name),
    JField("columns",JArray(i.columns.map(JString(_)).toList)),
    i.baseOrJoin.fold(t=> JField("base",t.name), j => JField("join", j)))
  )

  implicit def json2ImportTable(j:JValue):ImportTable= j match {
    case JObject(List(JField("name",JString(name)), JField("columns",JArray(columns)),JField(bORj,v))) => v match {
      case t:Table =>  ImportTable(name,columns.map(_.toString()), Left(t))
      case j:Join => ImportTable(name,columns.map(_.toString()), Right(j))
      case _ => sys.error("wrong implicit to ImportTable, neither Table or Join")
    }
    case _ => sys.error("wrong implicit to Import Table")
  }

  implicit def ingestInfo2Json(ii:IngestInfo):JValue=JArray(ii.tables.map(importTable2Json(_)).toList)
  implicit def json2IngestInfo(j:JValue):IngestInfo=j match {
    case JArray(vals) => IngestInfo(vals.map(json2ImportTable(_)))
    case _ => sys.error("wrong implicit to IngestInfo")
  }
}
