package com.precog.tools.importers.mongo

import com.mongodb.casbah.Imports._
import blueeyes.persistence.mongo.json.BijectionsMongoJson._
import blueeyes.persistence.mongo.json.BijectionsMongoJson.MongoToJson._
import com.mongodb.casbah.commons.TypeImports.ObjectId
import scalaz.{Monad, StreamT}
import akka.dispatch.{Await, Future}
import blueeyes.json.{JObject, JString, JParser, JValue}
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
import annotation.tailrec

/**
 * User: gabriel
 * Date: 1/17/13
 */
object ImportMongo {

  implicit val as=actorSystem
  implicit val executionContext = defaultFutureDispatch
  implicit val M: Monad[Future] = new FutureMonad(executionContext)

  def parseInt(s : String) : Option[Int] = try {
    Some(s.toInt)
  } catch {
    case _ : java.lang.NumberFormatException => None
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

    implicit val mongoConn= MongoConnection(mongoHost,mongoPort)

    @tailrec
    def readConfigLine(acc:List[String]):List[String]={
      val line=readLine()
      if (line != null && line != ""){
        if (line.startsWith("#")) readConfigLine(acc) //skip lines starting with #
        else readConfigLine(line::acc)
      } else acc
    }
    println("# enter json import descriptors, EOF or empty line to continue")
    println("""# format: { "database":"<database name>", "collection":"<database name>" } or { "database":"<database name>", "collection":"<database name>",  "lastId":"<last id>" } """)
    val jsonImputs=readConfigLine(Nil)
    val fresults=jsonImputs.map(JParser.parseFromString(_).map(importCollection(precogHost,basePath,apiKey,_))).flatMap(_.toList)

    val continueJson=Await.result(Future.sequence(fresults), Duration("24 hours"))
    println("#################################################################")
    println("# to continue ingestion from last point, use the following imput:")
    println(continueJson.mkString("\n"))

    actorSystem.shutdown()

  }

  def importCollection(precogHost:String, basePath:String, apiKey:String, jparams: JValue) (implicit mongoConn: MongoConnection):Future[String]={
    def strValue(jv: JValue) = (jv --> classOf[JString]).value
    val dbName = strValue(jparams \ "database")
    val collName = strValue(jparams \ "collection")
    val lastId = (jparams \? "lastId").map(strValue(_)) getOrElse ("000000000000000000000000")

    val fdsid = Future {
      readFromMongo(mongoConn, dbName, collName, lastId)
    }
    val (fds, fmaxId) = (fdsid map (_._1), fdsid map (_._2))
    val fjsons = fds.map(_.flatMap(MongoToJson(_).toStream))
    val fullPath = "%s/ingest/v1/sync/fs%s/%s".format(precogHost, basePath, collName)
    val data = StreamT.fromStream[Future, JValue](fjsons)
    val fresult = M.lift2((a: HttpResponse[ByteChunk], b: ObjectId) => (a, b))(sendToPrecog(fullPath, apiKey, data), fmaxId)

    fresult.map(r => {
      val (result, oid) = r
      result match {
        case HttpResponse(_, _, Some(Left(buffer)), _) => {
          println("### result from precog: %s".format(new String(buffer.array(), "UTF-8")))
        }
        case _ => println("### error: %s".format(result.toString()))
      }
      """{ "database":"%s", "collection":"%s"  "lastId":"%s" }""".format(dbName, collName, oid)
    })
  }

  def readFromMongo(mongoConn: MongoConnection, dbName: String, colName: String, oid:String):(Stream[DBObject],ObjectId)={
    val mongoDB = mongoConn(dbName)
    val mongoColl = mongoDB(colName)
    val q = "_id" $gt (new ObjectId(oid))
    val rStrm=mongoColl.find(q).toStream //.view ?
    val (oids,dataStrm)=rStrm.map(m=>(m.get("_id").asInstanceOf[ObjectId],m)).unzip
    (dataStrm,oids.max)
  }

  def sendToPrecog(fullPath:String, apiKey:String, dataStream:StreamT[Future,JValue]): Future[HttpResponse[ByteChunk]] = {

    val httpClient = new HttpClientXLightWeb()(defaultFutureDispatch)

    val byteStream: StreamT[Future, ByteBuffer] = dataStream.map(jv => ByteBuffer.wrap({
      val js = "%s\n".format(jv.renderCompact)
      print("# %s".format(js))
      js
    }.getBytes("UTF-8")))

    //get the last/biggest id
    val byteChunks: ByteChunk = Right(byteStream)
    httpClient.parameters('apiKey -> apiKey).post(fullPath)(byteChunks)
  }
}
