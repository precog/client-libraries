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

import rosetta.io.Serializer

/** A very simple client designed for requests and responses that fit entirely
 * in memory. If you need streaming, you shouldn't use this facade.
 */
trait HttpClient[T] { self =>
  def request(method: String, url: String, content: Option[T], headers: Map[String, String] = Map.empty[String, String]): T

  final def get(url: String, headers: Map[String, String] = Map.empty[String, String]): T = request("GET", url, None, headers)

  final def put(url: String, content: T, headers: Map[String, String] = Map.empty[String, String]): T = request("PUT", url, Some(content), headers)

  final def post(url: String, content: T, headers: Map[String, String] = Map.empty[String, String]): T = request("POST", url, Some(content), headers)

  final def delete(url: String, headers: Map[String, String] = Map.empty[String, String]): T = request("DELETE", url, None, headers)

  final def url(prefix: String): HttpClient[T] = new HttpClient[T] {
    def request(method: String, url: String, content: Option[T], headers: Map[String, String] = Map.empty[String, String]): T = {
      val prefix2 = if (prefix.endsWith("/")) prefix else prefix + "/"

      self.request(method, prefix2 + url, content, headers)
    }
  }

  final def queries(qs: (String, String)*): HttpClient[T] = if (qs.length == 0) self else new HttpClient[T] {
    import java.net.URLEncoder

    def request(method: String, url: String, content: Option[T], headers: Map[String, String] = Map.empty[String, String]): T = {
      self.request(method, addQueriesToUrl(url), content, headers)
    }

    private def addQueriesToUrl(url: String): String = {
      val index = url.indexOf('?')

      if (index >= 0) {
        if (index == url.length - 1) url + encodedQueries
        else url + "&" + encodedQueries
      }
      else url + "?" + encodedQueries
    }

    private lazy val encodedQueries: String = qs.map(t => t._1 + "=" + URLEncoder.encode(t._2, "UTF-8")).mkString("&")
  }

  final def query(query: (String, String)): HttpClient[T] = queries(query)

  final def headers(defHeaders: (String, String)*): HttpClient[T] = new HttpClient[T] {
    private lazy val defHeadersMap = defHeaders.toMap

    def request(method: String, url: String, content: Option[T], headers: Map[String, String] = Map.empty[String, String]): T = self.request(method, url, content, defHeadersMap ++ headers)
  }

  final def header(key: String, value: String): HttpClient[T] = headers((key, value))

  final def contentType[S](contentTypeValue: String)(implicit serializer: Serializer[S, T]): HttpClient[S] = (new HttpClient[S] {
    def request(method: String, url: String, content: Option[S], headers: Map[String, String] = Map.empty[String, String]): S = {
      val content2: Option[T]  = content.map(serializer.serialize _)

      val response: T = self.request(method, url, content2, headers)
      val response2: S = serializer.deserialize(response)

      println("content = " + content + ", content2 = " + content2)
      println("response = " + response + ", response2 = " + response2)

      response2
    }
  }).header("Content-Type", contentTypeValue)
}