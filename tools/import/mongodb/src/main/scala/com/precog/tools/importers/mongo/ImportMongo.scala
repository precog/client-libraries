package com.precog.tools.importers.mongo

import com.mongodb.casbah.Imports._
import blueeyes.persistence.mongo.json.BijectionsMongoJson._
import com.mongodb.casbah.commons.TypeImports.ObjectId
import scalaz._
import akka.dispatch.Future
import blueeyes.json._
import blueeyes.bkka.AkkaDefaults._
import blueeyes.bkka.AkkaDefaults.defaultFutureDispatch
import blueeyes.bkka.FutureMonad
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
import org.slf4j.LoggerFactory


/**
 * User: gabriel
 * Date: 1/17/13
 */
object ImportMongo {
/*
Next version:
every time it loads:
configuration exists?
if no, prompt to run config
if yes, check for new collections and prompt to config those

after run, update config with latest

use configrity for configuration
*/
  private lazy val logger = LoggerFactory.getLogger("com.precog.tools.importers.mongo.ImportMongo")

  implicit val as=actorSystem
  implicit val executionContext = defaultFutureDispatch
  implicit val M: Monad[Future] = new FutureMonad(executionContext)

  val collsConfig="precog_import_config"
  val sampleSize=100


  def matchValid[T](value:Any, fi: =>T, fl: =>T, ff: =>T, fd: =>T, fs: =>T,foid: =>T, fdt: =>T, fno: =>T= null)={
    value match {
      case i: java.lang.Integer => fi
      case l: java.lang.Long => fl
      case f: java.lang.Float => ff
      case d: java.lang.Double => fd
      case l: java.lang.String => fs
      case oid: ObjectId => foid
      case dt: Date => fdt
      case _ if fno != null => fno
    }
  }

  // No @tailrec but we don't expect getting back from mongoDb a hierarchy big enough to blow the stack
  def columnsOf(bObject: MongoDBObject): Seq[String]={
    bObject.flatMap(kv => kv._2 match {
      case m:MongoDBObject => columnsOf(m).map("%s.%s".format(kv._1,_))
      case _ => Set(kv._1)
    }).toSeq
  }

  def sampleColumns(mongoConn:MongoConnection)(db: MongoDB, coll: String):Set[String]={
    val collection=db(coll).find().take(sampleSize)
    collection.foldLeft(Set[String]())((s,o)=>s++(columnsOf(o)))
  }

  def configureCollections(mongoConn:MongoConnection)(db: MongoDB):Seq[DBObject]={
    println("No configuration found in the mongo instance, creating a new one.")
    println("DATABASE  %s \n".format(db.name))
    val userCollections=db.getCollectionNames().filter(name=> !(name.startsWith("system.") || name.startsWith(collsConfig)))
    val colls=selectSet("collection",userCollections.toSeq)
    colls.map( coll =>{
      println("\n ---- Collection %s ----".format(coll))
      val columns=sampleColumns(mongoConn)(db,coll).toSeq
      val fields=selectSet("column", columns)

      val sortColumns=db(coll).find().take(sampleSize).map(mobj => mobj.toMap).reduceLeft(_++_).filter( kv =>  matchValid(kv._2, true, true, true, true, true, true, true, false ))

      val sortColumn=selectOne("import control column", sortColumns.keys.toSeq)
      MongoDBObject("collection"->coll, "fields"->fields, "sortColumn"->sortColumn)
      }
    )
  }

  def pair[T](getter: String=>T)(name:String ) = (name-> getter(name))

  def getString(jo: JObject)(field:String) = strValue(jo \ field)
  def getArray(jo: JObject)(field:String) = arrOfStrValues(jo \ field)

  def strValue(jv: JValue) = (jv --> classOf[JString]).value
  def arrOfStrValues(jv: JValue) = (jv -->? classOf[JArray]).map(_.elements.map(strValue(_))).getOrElse(Nil)


  def importCollection(host:String, basePath:String, apiKey:String, db:MongoDB, mdbobj: MongoDBObject, mongoConn: MongoConnection):Future[(Either[String,String],AnyRef)]={

    val collName = mdbobj.getAs[String]("collection").get
    val fieldNames = mdbobj.getAsOrElse[util.ArrayList[String]]("fields",new util.ArrayList())
    val lastId = mdbobj.getAs[String]("lastId")
    val sortColumn=mdbobj.getAs[String]("sortColumn").get

    logger.info("Ingesting %s since %s of %s".format(collName,lastId,sortColumn))

    val fdsid = Future { dsZipMaxIds(db, collName, sortColumn, fieldNames, lastId) }
    val (fds, fmaxId) = (fdsid map (_._1), fdsid map (_._2))

    val fjsons = fds.map(_.flatMap(MongoToJson(_).toStream))
    val path = "%s/%s/%s".format(basePath, db.name, collName)
    val data = StreamT.fromStream[Future, JObject](fjsons)
    val fsend= data.isEmpty.flatMap( isEmpty =>
      if (isEmpty) Future(Left("No new data found in %s.%s".format(db.name,collName)))
      else sendToPrecog(host,path,apiKey,toByteStream(data),streaming=false) flatMap( _ match {
          case HttpResponse(status, _, Some(Left(buffer)), _) => {
            Future(Right("Result from precog: %s (%s)".format(new String(buffer.array(), "UTF-8"), status)))
          }
          case HttpResponse(_, _, Some(Right(stream)), _) => {
            stream.toStream.map( strmBuffer =>Right("Streaming result from precog: %s".format(strmBuffer.foldLeft("")( (str,b)=> str+new String(b.array(), "UTF-8")))))
          }
          case result => Future(Left("Error: %s".format(result.toString())))
        }
      ))
    M.lift2((a: Either[String,String], b: AnyRef) => (a, b))(fsend, fmaxId)
  }


  def dsZipMaxIds(db: MongoDB, collName: String, sortColumn: String, fieldNames: util.ArrayList[String], lastId: Option[String]): (Stream[DBObject], AnyRef) = {
    val rStrm = readFromMongo(db, collName, sortColumn, fieldNames, lastId)
    val (oids, dataStrm) = rStrm.map(m => (m(sortColumn), m)).unzip


    val maxOid = if (oids.isEmpty) lastId
    else {

      //ugly but need the runtime type to go form AnyRef to Ordering[_] for max to work... sum types + def ordering for sum types?
        def refine[T<:Comparable[T]](f: AnyRef=>T)=oids.map(f).max
      matchValid(oids.head,
        refine({case ss:java.lang.Integer => ss}),
        refine({case ss:java.lang.Long => ss}),
        refine({case ss:java.lang.Float => ss}),
        refine({case ss:java.lang.Double => ss}),
        refine({case ss:String => ss}),
        refine({case ss:ObjectId => ss}),
        refine({case ss:Date => ss}))
      }
    (dataStrm, maxOid)
  }

  def readFromMongo[A : AsQueryParam](mongoDB: MongoDB, collName: String, idCol:String, fieldNames:Seq[String], oLastId:Option[A]=None:Option[ObjectId]):Stream[DBObject]={
    val mongoColl = mongoDB(collName)
    val q = oLastId.map( idCol $gt  _ ).getOrElse(MongoDBObject())
    val fields = MongoDBObject(fieldNames.map(_->""):_*)
    mongoColl.find(q,fields).toStream
  }

  def main(args:Array[String]){

    if (args.length != 4) {
      println("Wrong number of parameters.")
      println("Usage: ImportMongo mongo_uri precog_host_url precog_ingest_path precog_apiKey")
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

        val fimports=jsonInputs.map(config=> {

          val collName = config.getAs[String]("collection").get
          val lastId = config.getAs[String]("lastId")
          val sortColumn=config.getAs[String]("sortColumn").get
          println("Ingesting %s since %s of %s".format(collName,lastId,sortColumn))

          importCollection(precogHost,basePath,apiKey,db, config, mongoConn)
        })

        Future.sequence(fimports).onComplete( x => {x match {
          case Right(results) => {
            jsonInputs.zip(results).map(  {case (mDbObj,(result,lastId)) =>
              result.left.map(s=>{
                  val result="%s".format(s)
                  logger.warn(result)
                  println(result)
                }
              ).right.map(s=>{
                  inputConfigColl.save(mDbObj++("lastId"->lastId))
                  val result="%s".format(s)
                  logger.info(result)
                  println(result)
                }
              )
            }
            )
          }
          case Left(e) =>  logger.error("Exception during import ",e)
          }
          actorSystem.shutdown()
        }
        )
      }
    } catch {
      case e:Throwable => {
        logger.error("General exception during import",e)
        actorSystem.shutdown()
      }
    }
  }
}
