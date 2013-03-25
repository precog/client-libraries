package com.precog.tools.importers.jdbc

import blueeyes.core.service.test.BlueEyesServiceSpecification
import blueeyes.core.data._
import blueeyes.core.http.test.HttpRequestMatchers
import blueeyes.core.service._
import blueeyes.core.data.DefaultBijections._
import java.sql.DriverManager
import akka.dispatch.{Future, Await}
import blueeyes.core.http.HttpResponse
import JsonImplicits._
import scalaz.Monad
import blueeyes.bkka.{AkkaDefaults, FutureMonad}

/**
 * User: gabriel
 * Date: 12/4/12
 */
class ImportJdbcServiceTest extends BlueEyesServiceSpecification with ImportJdbcService with HttpRequestMatchers with AkkaDefaults {

  val executionContext = defaultFutureDispatch
  implicit val M: Monad[Future]  = new FutureMonad(executionContext)

  override val host="https://devapi.precog.com"

  def dbUrl(db:String)="jdbc:h2:~/%s".format(db)

  "Database metadata" should {
    "get database metadata" in new Conn{ val dbName ="TESTSVC"
      tblA

      val r=client.parameters('dbUrl-> dbUrl(dbName)).get[ByteChunk]("/JdbcImportService/v1/metadata/databases")
      Await.result(r,1 minute) must beLike {
        case HttpResponse(_ ,_,Some(Left(buffer)),_) => new String(buffer.array(), "UTF-8") must_== """["TESTSVC"]"""
      }
    }
  }

  "Table metadata" should {
    "get tables " in new Conn{ val dbName ="tmd"
      tblA;  tblB
      val r=client.parameters('dbUrl-> dbUrl(dbName)).get[ByteChunk]("/JdbcImportService/v1/metadata/databases/%s/tables".format(dbName))
      Await.result(r,1 minute) must beLike {
        case HttpResponse(_ ,_,Some(Left(buffer)),_) => new String(buffer.array(), "UTF-8") must_== """["A","B"]"""
      }
    }

    "get single table desc w/o relations" in new Conn{ val dbName ="t1wor"
      tblA;tblB
      val r= client.parameters('dbUrl-> dbUrl(dbName)).get[ByteChunk]("/JdbcImportService/v1/metadata/databases/%s/tables/A".format(dbName))
      Await.result(r,1 minute) must beLike {
        case HttpResponse(_ ,_,Some(Left(buffer)),_) => new String(buffer.array(), "UTF-8") must_== """[{"name":"A","columns":["ID","NAME"],"base":"A"}]"""
      }
    }

    "get single table desc with declared relations" in new Conn{ val dbName ="t1wdr"
      tblA;tblB
      cnstrBfkA

      val r=client.parameters('dbUrl-> dbUrl(dbName)).get[ByteChunk]("/JdbcImportService/v1/metadata/databases/%s/tables/A".format(dbName))
      Await.result(r,1 minute) must beLike {
        case HttpResponse(_ ,_,Some(Left(buffer)),_) => new String(buffer.array(), "UTF-8") must_==
          """[{"name":"A","columns":["ID","NAME"],"base":"A"},{"name":"B","columns":["ID","A_ID","NAME"],"join":{"baseColName":"ID","refKey":{"table":"B","columnName":"A_ID"},"exported":true}}]"""
      }
    }

    "get table desc with inferred relations w/o sampling" in new Conn{ val dbName ="t1wir"
      tblA;tblB

      val r=client.parameters('dbUrl-> dbUrl(dbName),'infer->"y").get[ByteChunk]("/JdbcImportService/v1/metadata/databases/%s/tables/A".format(dbName))
      Await.result(r,1 minute) must beLike {
        case HttpResponse(_ ,_,Some(Left(buffer)),_) => new String(buffer.array(), "UTF-8") must_==
          """[{"name":"A","columns":["ID","NAME"],"base":"A"},{"name":"B","columns":["ID","A_ID","NAME"],"join":{"baseColName":"ID","refKey":{"table":"B","columnName":"A_ID"},"exported":true}}]"""
      }
    }

    "get table desc with inferred relations with sampling - no data" in new Conn{ val dbName = "t1wirsnd"
      tblA;tblB

      val r=client.parameters('dbUrl-> dbUrl(dbName),'infer->"y", 'sample->"y").get[ByteChunk]("/JdbcImportService/v1/metadata/databases/%s/tables/A".format(dbName))
      Await.result(r,1 minute) must beLike {
        case HttpResponse(_ ,_,Some(Left(buffer)),_) => new String(buffer.array(), "UTF-8") must_==
          """[{"name":"A","columns":["ID","NAME"],"base":"A"}]"""
      }
    }

    "get table desc with inferred relations with sampling - with data" in new Conn{ val dbName ="t1wirsd"
      tblA;tblB; dataA; dataB

      val r=client.parameters('dbUrl-> dbUrl(dbName),'infer->"y", 'sample->"y").get[ByteChunk]("/JdbcImportService/v1/metadata/databases/%s/tables/A".format(dbName))
      Await.result(r,1 minute) must beLike {
        case HttpResponse(_ ,_,Some(Left(buffer)),_) => new String(buffer.array(), "UTF-8") must_==
          """[{"name":"A","columns":["ID","NAME"],"base":"A"},{"name":"B","columns":["ID","A_ID","NAME"],"join":{"baseColName":"ID","refKey":{"table":"B","columnName":"A_ID"},"exported":true}}]"""
      }
    }

  }

  "Ingest data" should {
    "ingest with query" in new Conn{ val dbName ="iwq"
      tblA;tblB; dataA; dataB
      val r=client.parameters(
        'dbUrl-> dbUrl(dbName),
        'objName->"baseObj",
        'q->"select * from A,B where A.ID = B.A_ID",
        'apiKey->apiKey,
        'path -> basePath
      ).post[ByteChunk]("/JdbcImportService/v1/ingest/%s/query".format(dbName))(Array.empty[Byte])
      Await.result(r,1 minute) must beLike {
        case HttpResponse(_ ,_,Some(Left(buffer)),_) => new String(buffer.array(), "UTF-8") must_==
          """{"ingested":1,"errors":[]}"""
      }
    }

    "ingest auto denormalize declared " in new Conn{ val dbName ="iauto"
      tblA;tblB; dataA; dataB
      val r=client.parameters(
        'dbUrl-> dbUrl(dbName),
        'denormalize->"y",
        'apiKey->apiKey,
        'path -> basePath
      ).post[ByteChunk]("/JdbcImportService/v1/ingest/%s/table/%s/auto".format(dbName,"A"))(Array.empty[Byte])
      Await.result(r,1 minute) must beLike {
        case HttpResponse(_ ,_,Some(Left(buffer)),_) => new String(buffer.array(), "UTF-8") must_==
          """{"ingested":1,"errors":[]}"""
      }
    }

    "ingest with config" in new Conn{ val dbName ="iwcfg"
      import DefaultBijections.jvalueToChunk
      tblA;tblB; dataA; dataB

      val r=client.parameters(
        'dbUrl-> dbUrl(dbName),
        'apiKey->apiKey,
        'path -> basePath
      ).post[ByteChunk]("/JdbcImportService/v1/ingest/%s/table/%s/config".format(dbName,"A"))(ingestInfo2Json(tblABDesc))
      Await.result(r,2 minute) must beLike {
        case HttpResponse(_ ,_,Some(Left(buffer)),_) => new String(buffer.array(), "UTF-8") must_==
          """{"ingested":1,"errors":[]}"""
      }
    }
  }

}
