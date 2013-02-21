package com.precog.tools.importers.mongo

import com.mongodb.casbah.Imports._
import blueeyes.persistence.mongo.json.BijectionsMongoJson._
import com.mongodb.casbah.commons.TypeImports.ObjectId
import scalaz._
import akka.dispatch.{Await, Future}
import blueeyes.json._
import blueeyes.core.service.engines.HttpClientXLightWeb
import blueeyes.bkka.AkkaDefaults._
import blueeyes.bkka.AkkaDefaults.defaultFutureDispatch
import blueeyes.bkka.FutureMonad
import blueeyes.core.data.ByteChunk
import java.nio.ByteBuffer
import akka.util.Duration
import blueeyes.core.http.HttpResponse
import blueeyes.core.service._
import com.precog.tools.importers.common._
import ConsoleUtils._
import com.mongodb
import mongodb.casbah.MongoURI
import mongodb.casbah.query.AsQueryParam
import java.util.Date
import java.util
import collection.JavaConversions._

//import mongodb.casbah.commons.Imports._
import scala.Left
import scala.Some
import scala.Right


/**
 * User: gabriel
 * Date: 1/17/13
 */
object ImportMongo {

  implicit val as=actorSystem
  implicit val executionContext = defaultFutureDispatch
  implicit val M: Monad[Future] = new FutureMonad(executionContext)

  val collsConfig="precog_import_config"
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

  def sampleColumns(db: MongoDB, coll: String)(implicit mongoConn:MongoConnection)={
    val collection=db(coll).find().take(sampleSize)
    collection.flatMap(columnsOf(_)).toSet
  }

  def configureCollections(db: MongoDB)(implicit mongoConn:MongoConnection):Seq[DBObject]={
    println("No configuration found in the mongo instance, creating a new one.")
    val databases=db.name
    println("DATABASE  %s \n".format(db))
    val colls=selectSet("collection",db.getCollectionNames().toSeq)
    colls.map( coll =>{
      println("\n ---- Collection %s ----".format(coll))
      val columns=sampleColumns(db,coll).toSeq
      val fields=selectSet("column", columns)

      //TODO ugly, maybe using a wrapper type?
      val sortColumns=db(coll).find().take(sampleSize).map(mobj => mobj.toMap).reduceLeft(_++_).filter( kv => kv._2 match {
        case s:String => true
        case d:java.lang.Long => true
        case oid:ObjectId =>  true
        case dt:Date => true
        case _ => false
      })

      val sortColumn=selectOne("import control column", sortColumns.keys.toSeq)
      MongoDBObject("collection"->coll, "fields"->fields, "sortColumn"->sortColumn)
      }
    )
  }

  def main(args:Array[String]){

    if (args.length != 4) {
      println("Wrong number of parameters.")
      println("Usage: ImportMongo mongo_uri precog_host precog_ingest_path precog_apiKey")
      actorSystem.shutdown()
      sys.exit(1)
    }

    val mongoUri=args(0)

    val precogHost=args(1)
    val basePath=args(2)
    val apiKey=args(3)
    try {
      val uri = MongoURI(mongoUri)

      implicit val mongoConn=MongoConnection(uri)
      uri.database.map { database =>

        //TODO: use uri.database.asList and if it's empty, load the full list of dbs
        val db = mongoConn(database)
        for {
          user <- uri.username
          password <- uri.password
        } {
          db.authenticate(user, password.mkString)
        }

        val inputConfigColl=db(collsConfig)


        if (inputConfigColl.isEmpty) {
          val configs=configureCollections(db)
          configs.map(inputConfigColl.save(_))
        }
        val jsonImputs= inputConfigColl.find().toList

        val fimports=jsonImputs.map(config=> importCollection(precogHost,basePath,apiKey,db, config))

        val fresults=Await.result(Future.sequence(fimports.toList), Duration("24 hours"))

        jsonImputs.zip(fresults).map( r =>{
            val (mDbObj,(result,lastId)) = r
            println("%s".format(result))
            inputConfigColl.save(mDbObj++("lastId"->lastId))
          }
        )
      }
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


  def importCollection(precogHost:String, basePath:String, apiKey:String, db:MongoDB, mdbobj: MongoDBObject) (implicit mongoConn: MongoConnection):Future[(String,AnyRef)]={
    //val jparams: JObject=MongoToJson(mdbobj)
    //val dbName = mdbobj.getAs[String]("database").get//getString(jparams)("database")
    val collName = mdbobj.getAs[String]("collection").get//getString(jparams)("collection")
    val fieldNames = mdbobj.getAsOrElse[util.ArrayList[String]]("fields",new util.ArrayList())//getArray(jparams)("fields")   MongoDB
    val lastId = mdbobj.getAs[String]("lastId") //(jparams \? "lastId").map(strValue(_)) getOrElse ("000000000000000000000000")
    val sortColumn=mdbobj.getAs[String]("sortColumn").get
    val fdsid = Future {
      val rStrm=readFromMongo(db, collName, sortColumn, lastId, fieldNames)
      val (oids,dataStrm)=rStrm.map(m=>(m(sortColumn),m)).unzip

      //ugly but need the runtime type to go form AnyRef to Ordering[_] for max to work... sum types + def ordering for sum types?
      val maxOid= if (oids.isEmpty) lastId else {
        oids.head match {
          case s:String => oids.map( {case ss:String => ss}).max
          case d:java.lang.Long => oids.map( {case ds:java.lang.Long => ds}).max
          case oid:ObjectId =>  oids.map( {case oids:ObjectId => oids}).max
          case dt:Date => oids.map( {case ds:Date => ds}).max
        }
      }
      (dataStrm,maxOid)
    }
    val (fds, fmaxId) = (fdsid map (_._1), fdsid map (_._2))

    val fjsons = fds.map(_.flatMap(MongoToJson(_).toStream))
    val fullPath = "%s/ingest/v1/sync/fs%s/%s/%s".format(precogHost, basePath, db.name, collName)
    val data = StreamT.fromStream[Future, JObject](fjsons)
    val fsend=data.isEmpty.flatMap( isEmpty =>
      if (isEmpty) Future("No new data found in %s.%s".format(db.name,collName))
      else sendToPrecog(fullPath, apiKey, data)map( _ match {
          case HttpResponse(_, _, Some(Left(buffer)), _) => {
            "Result from precog: %s".format(new String(buffer.array(), "UTF-8"))
          }
          case result => "Error: %s".format(result.toString())
        }
      ))
    M.lift2((a: String, b: AnyRef) => (a, b))(fsend, fmaxId)
  }

  def readFromMongo(mongoDB: MongoDB, collName: String, idCol:String, oLastId:Option[AnyRef], fieldNames:Seq[String]):Stream[DBObject]={
    val mongoColl = mongoDB(collName)

    //ugly, maybe using a wrapper type?
    val q = oLastId.map(
      _ match {
        case s:String => idCol $gt s
        case d:java.lang.Long => idCol $gt d.longValue()
        case oid:ObjectId =>  idCol $gt oid
        case dt:Date => idCol $gt dt
      }
    ).getOrElse(MongoDBObject())
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

    val byteChunks: ByteChunk = Right(byteStream)
    httpClient.parameters('apiKey -> apiKey).post(fullPath)(byteChunks)
  }
}
