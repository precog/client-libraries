/* Copyright (C) 2011 by ReportGrid, Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * No portion of this Software shall be used in any application which does not
 * use the ReportGrid platform to provide some subset of its functionality.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.reportgrid.api

import org.apache.http.{HttpResponse}
import org.apache.http.impl.client.{DefaultHttpClient}
import org.apache.http.client.{ResponseHandler}
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager
import org.apache.http.client.methods._
import org.apache.http.entity._
import org.apache.http.util.EntityUtils

class HttpClientApache extends HttpClient[String] {
  private val client = new DefaultHttpClient(new ThreadSafeClientConnManager)

  def request(method: String, url: String, content: Option[String], headers: Map[String, String] = Map.empty[String, String]): String = {
    //println("method  = " + method);
    //println("url     = " + url);
    //println("content = " + content);
    //println("headers = " + headers.mkString(", "))

    val request = new HttpEntityEnclosingRequestBase {
      def getMethod = method.toUpperCase
    }

    request.setURI(new java.net.URI(url))

    headers.foreach { tuple =>
      val (name, value) = tuple

      request.setHeader(name, value)
    }

    content.foreach { content =>
      request.setEntity(new StringEntity(content))
    }

    val result: Either[Exception, String] = client.execute(request, new ResponseHandler[Either[Exception, String]] {
      def handleResponse(response: HttpResponse): Either[Exception, String] = {
        val statusLine = response.getStatusLine

        if (statusLine.getStatusCode != 200) {
          Left(new Exception("HTTP " + method + " " + url + " (" +
            headers.mkString(", ") + "): [" + content.map(_.toString).getOrElse("") + "]: " + statusLine.getReasonPhrase))
        } else {
          Right(EntityUtils.toString(response.getEntity))
        }
      }
    })

    result match {
      case Left(exception) => throw exception

      case Right(response) => response
    }
  }
}
