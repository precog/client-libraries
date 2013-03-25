package com.precog.tools.importers.mongo

import com.mongodb.casbah.Imports._
import blueeyes.persistence.mongo.json.BijectionsMongoJson._
import com.mongodb.casbah.commons.TypeImports.ObjectId
import scalaz._
import akka.dispatch.{Await, Future}
import blueeyes.json._
import blueeyes.bkka.AkkaDefaults._
import blueeyes.bkka.AkkaDefaults.defaultFutureDispatch
import blueeyes.bkka.FutureMonad
import akka.util.Duration
import blueeyes.core.http.HttpResponse
import com.precog.tools.importers.common._
import ConsoleUtils._
import com.mongodb
import mongodb.casbah.MongoURI
import java.util.Date
import java.util
import collection.JavaConversions._

import scala.Left
import scala.Some
import com.precog.tools.importers.common.Ingest._


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

  def sampleColumns(mongoConn:MongoConnection)(db: MongoDB, coll: String)={
    val collection=db(coll).find().take(sampleSize)
    collection.flatMap(columnsOf(_)).toSet
  }

  def configureCollections(mongoConn:MongoConnection)(db: MongoDB):Seq[DBObject]={
    println("No configuration found in the mongo instance, creating a new one.")
    val databases=db.name
    println("DATABASE  %s \n".format(db))
    val colls=selectSet("collection",db.getCollectionNames().toSeq)
    colls.map( coll =>{
      println("\n ---- Collection %s ----".format(coll))
      val columns=sampleColumns(mongoConn)(db,coll).toSeq
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

      val mongoConn=MongoConnection(uri)
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
          val configs=configureCollections(mongoConn)(db)
          configs.map(inputConfigColl.save(_))
        }
        val jsonInputs= inputConfigColl.find().toList

        //TODO: check result of ingest before updating the Id!!!!!
        val fimports=jsonInputs.map(config=> importCollection(precogHost,basePath,apiKey,db, config, mongoConn))

        val fresults=Await.result(Future.sequence(fimports.toList), Duration("24 hours"))

        jsonInputs.zip(fresults).map( r =>{
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


  def importCollection(host:String, basePath:String, apiKey:String, db:MongoDB, mdbobj: MongoDBObject, mongoConn: MongoConnection):Future[(String,AnyRef)]={

    val collName = mdbobj.getAs[String]("collection").get
    val fieldNames = mdbobj.getAsOrElse[util.ArrayList[String]]("fields",new util.ArrayList())
    val lastId = mdbobj.getAs[String]("lastId")
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
    val path = "%s/%s/%s".format(basePath, db.name, collName)
    val data = StreamT.fromStream[Future, JObject](fjsons)
    val fsend= data.isEmpty.flatMap( isEmpty =>
      if (isEmpty) Future("No new data found in %s.%s".format(db.name,collName))
      else sendToPrecog(host,path,apiKey,toByteStream(data)) map( _ match {
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
}
