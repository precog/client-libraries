package com.precog.tools.importers.common

import akka.dispatch._
import akka.dispatch.Future
import scalaz.Monad

import blueeyes.json._
import blueeyes.core.data.DefaultBijections._
import blueeyes.core.service._
import blueeyes.core.service.engines.HttpClientXLightWeb
import blueeyes.bkka.FutureMonad
import scalaz.StreamT
import java.nio.ByteBuffer
import blueeyes.core.http.HttpResponse
import blueeyes.core.data.ByteChunk
import scala.Right
import org.slf4j.LoggerFactory

/**
 * User: gabriel
 * Date: 3/21/13
 */
object Ingest {

  private lazy val logger = LoggerFactory.getLogger("com.precog.tools.importers.jdbc.Ingest")

  def sendToPrecog(host:String, path:String, apiKey:String, dataStream:StreamT[Future,ByteBuffer], streaming:Boolean = true)(implicit executor:ExecutionContext): Future[HttpResponse[ByteChunk]] = {
    implicit val M = new FutureMonad(executor)
    val httpClient = new HttpClientXLightWeb()(executor)

    dataStream.isEmpty.flatMap( isEmpty =>
      if (isEmpty) {
        logger.info("No need to send empty data stream")
        Future(HttpResponse.empty)
      } else {
        val byteChunks: ByteChunk = Right(dataStream)
        //val fullPath = "%s/ingest/v1/fs%s".format(host, path)
        val fullPath = "%s/fs%s".format(host, path) //local test only
        val ingestParams = ('apiKey -> apiKey)::( if (streaming) List('mode -> "streaming") else List('mode -> "batch", 'receipt -> "true"))
        logger.info("Ingesting to %s".format(path))
        httpClient.parameters(ingestParams:_*).header("Content-Type","application/json").post(fullPath)(byteChunks)
      }
    )
  }

  def callSucceded(response:HttpResponse[ByteChunk]){
    response match {
      case HttpResponse(_ ,_,Some(Left(buffer)),_) => logger.info("Result: %s".format(new String(buffer.array(), "UTF-8")))
      case _ => logger.error("Unexpected stream in %s".format(response))
    }
  }


  def toByteStream(dataStream: StreamT[Future, JValue])(implicit m:Monad[Future]): StreamT[Future, ByteBuffer] = {
    dataStream.map(jv => ByteBuffer.wrap({
      val js = "%s\n".format(jv.renderCompact)
      logger.trace("to bytes = %s".format(js.replace('\n',' ')))
      js
    }.getBytes("UTF-8")))
  }
}
