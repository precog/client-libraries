package com.precog.tools.importers.mongo

import com.mongodb.casbah.Imports._
import blueeyes.persistence.mongo.json.BijectionsMongoJson._
import blueeyes.persistence.mongo.json.BijectionsMongoJson.MongoToJson._
import com.mongodb.casbah.commons.TypeImports.ObjectId
import scalaz.{Monad, StreamT}
import akka.dispatch.{Await, Future}
import blueeyes.json._
import blueeyes.core.service.engines.HttpClientXLightWeb
import blueeyes.bkka.AkkaDefaults._
import blueeyes.core.data.DefaultBijections._
import blueeyes.bkka.AkkaDefaults.defaultFutureDispatch
import blueeyes.bkka.FutureMonad
import blueeyes.core.data.ByteChunk
import java.nio.ByteBuffer
import akka.util.Duration
import blueeyes.core.http.HttpResponse
import blueeyes.core.service._
import com.precog.tools.importers.common._
import ConsoleUtils._
import scala.Left
import scala.Some
import scala.Right
import scala.Left
import scala.Some
import scala.Right
import com.mongodb
import collection.JavaConversions.SeqWrapper


/**
 * User: gabriel
 * Date: 1/17/13
 */
object ImportMongo {

  implicit val as=actorSystem
  implicit val executionContext = defaultFutureDispatch
  implicit val M: Monad[Future] = new FutureMonad(executionContext)

  val configDb="_precog_mongo_importer"
  val collsConfig="collections_to_import"
  val sampleSize=100

  def parseInt(s : String) : Option[Int] = try {
    Some(s.toInt)
  } catch {
    case _ : java.lang.NumberFormatException => None
  }

  // No @tailrec but we don't expect getting back from mongoDb a hierarchy big enough to blow the stack
  def columnsOf(bObject: MongoDBObject): Seq[String]={
    bObject.flatMap(kv => kv._2 match {
      case m:MongoDBObject => columnsOf(m).map("%s.%s".format(kv._1,_))
      case _ => Set(kv._1)
    }).toSeq
  }

  def sampleColumns(db: String, coll: String)(implicit mongoConn:MongoConnection)={
    val collection=mongoConn(db)(coll).find().take(sampleSize)
    collection.flatMap(columnsOf(_)).toSet
  }

  def configureCollections(connection: MongoConnection):Seq[DBObject]={
    implicit val c=connection
    println("No configuration found in the mongo instance, creating a new one.")
    val databases=selectSet("database",connection.databaseNames)
    val dbColls=databases.map( db=>{ println("Database %s".format(db)); (db,selectSet("collection",connection(db).getCollectionNames().toSeq))})
    dbColls.flatMap(dbColl =>{
        val (db,colls) = dbColl
        colls.map( coll =>{
          val fields=if (readLine("Sample and select columns of %s.%s? (y/N)".format(db,coll)).toLowerCase == "y"){
              Some(selectSet("column", sampleColumns(db,coll).toSeq ))
            } else {
              None
            }
          val dbObj =MongoDBObject("database"->db, "collection"->coll)
          fields.map(flds=>dbObj ++ ("fields"->flds)).getOrElse(dbObj)
          }
        )
      }
    )
  }

  def main(args:Array[String]){

    if (args.length != 5) {
      println("Wrong number of parameters.")
      println("Usage: ImportMongo mongo_host mongo_port precog_host precog_ingest_path precog_apiKey")
      actorSystem.shutdown()
      sys.exit(1)
    }

    val mongoHost=args(0)
    val mongoPort=parseInt(args(1)).get

    val precogHost=args(2)
    val basePath=args(3)
    val apiKey=args(4)
    try {
      implicit val mongoConn= MongoConnection(mongoHost,mongoPort)

      val inputConfigColl=mongoConn(configDb)(collsConfig)

      //workaround
      if (inputConfigColl.isEmpty) {
        val configs=configureCollections(mongoConn)
        configs.map(inputConfigColl.save(_))
      }
      val jsonImputs= inputConfigColl.find().toList

      val fimports=jsonImputs.flatMap(x=> MongoToJson(x).toList.map(importCollection(precogHost,basePath,apiKey,_)))

      val fresults=Await.result(Future.sequence(fimports), Duration("24 hours"))

      jsonImputs.zip(fresults).map( r =>{
          val (mDbObj,(result,lastId)) = r
          println("%s".format(result))
          inputConfigColl.save(mDbObj++("lastId"->lastId)) //JsonToMongo(continueJson).map(inputConfigColl.save(_))
        }
      )
    } finally {
      println("Shutting down...")
      actorSystem.shutdown()
    }
  }

  def pair[T](getter: String=>T)(name:String ) = (name-> getter(name))

  def getString(jo: JObject)(field:String) = strValue(jo \ field)
  def getArray(jo: JObject)(field:String) = arrOfStrValues(jo \ field)

  def strValue(jv: JValue) = (jv --> classOf[JString]).value
  def arrOfStrValues(jv: JValue) = (jv -->? classOf[JArray]).map(_.elements.map(strValue(_))).getOrElse(Nil)

  def importCollection(precogHost:String, basePath:String, apiKey:String, jparams: JObject) (implicit mongoConn: MongoConnection):Future[(String,String)]={
    val dbName = getString(jparams)("database")
    val collName = getString(jparams)("collection")
    val fieldNames = getArray(jparams)("fields")
    val lastId = (jparams \? "lastId").map(strValue(_)) getOrElse ("000000000000000000000000")
    val fdsid = Future {
      val rStrm=readFromMongo(mongoConn, dbName, collName, lastId, fieldNames)
      val (oids,dataStrm)=rStrm.map(m=>(m.get("_id").asInstanceOf[ObjectId],m)).unzip
      val maxOid= if (oids.isEmpty) lastId else oids.max.toStringMongod
      (dataStrm,maxOid)
    }
    val (fds, fmaxId) = (fdsid map (_._1), fdsid map (_._2))

    val fjsons = fds.map(_.flatMap(MongoToJson(_).toStream))
    val fullPath = "%s/ingest/v1/sync/fs%s/%sr/%s".format(precogHost, basePath, dbName, collName)
    val data = StreamT.fromStream[Future, JObject](fjsons)
    val fsend=data.isEmpty.flatMap( isEmpty =>
      if (isEmpty) Future("No new data found in %s.%s".format(dbName,collName))
      else sendToPrecog(fullPath, apiKey, data)map( _ match {
          case HttpResponse(_, _, Some(Left(buffer)), _) => {
            "Result from precog: %s".format(new String(buffer.array(), "UTF-8"))
          }
          case result => "Error: %s".format(result.toString())
        }
      ))
    M.lift2((a: String, b: String) => (a, b))(fsend, fmaxId)
  }

  def readFromMongo(mongoConn: MongoConnection, dbName: String, colName: String, oid:String, fieldNames:Seq[String]):Stream[DBObject]={
    val mongoDB = mongoConn(dbName)
    val mongoColl = mongoDB(colName)
    val q = "_id" $gt (new ObjectId(oid))
    val fields = MongoDBObject(fieldNames.map(_->""):_*)
    mongoColl.find(q,fields).toStream //.view ?
  }

  def sendToPrecog(fullPath:String, apiKey:String, dataStream:StreamT[Future,JValue]): Future[HttpResponse[ByteChunk]] = {

    val httpClient = new HttpClientXLightWeb()(defaultFutureDispatch)

    val byteStream: StreamT[Future, ByteBuffer] = dataStream.map(jv => ByteBuffer.wrap({
      val js = "%s\n".format(jv.renderCompact)
      print("%s".format(js))
      js
    }.getBytes("UTF-8")))

    //get the last/biggest id
    val byteChunks: ByteChunk = Right(byteStream)
    httpClient.parameters('apiKey -> apiKey).post(fullPath)(byteChunks)
  }
}
