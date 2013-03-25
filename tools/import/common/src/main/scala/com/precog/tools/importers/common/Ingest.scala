package com.precog.tools.importers.common

import akka.dispatch._
import akka.dispatch.Future
import scalaz.Monad

import blueeyes.json._
import blueeyes.core.data.DefaultBijections._
import blueeyes.core.service._
import blueeyes.bkka.AkkaDefaults.defaultFutureDispatch
import blueeyes.core.service.engines.HttpClientXLightWeb
import blueeyes.bkka.FutureMonad
import scalaz.StreamT
import java.nio.ByteBuffer
import blueeyes.core.http.HttpResponse
import blueeyes.core.data.ByteChunk
import scala.Right

/**
 * User: gabriel
 * Date: 3/21/13
 */
object Ingest {

  def sendToPrecog(host:String, path:String, apiKey:String, dataStream:StreamT[Future,ByteBuffer])(implicit ec:ExecutionContext): Future[HttpResponse[ByteChunk]] = {
    implicit val M = new FutureMonad(ec)
    val httpClient = new HttpClientXLightWeb()(defaultFutureDispatch)

    dataStream.isEmpty.flatMap( isEmpty =>
      if (isEmpty) Future(HttpResponse.empty)
      else {
        val byteChunks: ByteChunk = Right(dataStream)
        val fullPath = "%s/ingest/v1/fs%s".format(host, path)
        httpClient.parameters('apiKey -> apiKey,'mode -> "streaming").header("Content-Type","application/json").post(fullPath)(byteChunks)
      }
    )
  }


  def toByteStream(dataStream: StreamT[Future, JValue])(implicit m:Monad[Future]): StreamT[Future, ByteBuffer] = {
    dataStream.map(jv => ByteBuffer.wrap({
      val js = "%s\n".format(jv.renderCompact)
      print("%s".format(js))
      js
    }.getBytes("UTF-8")))
  }
}
