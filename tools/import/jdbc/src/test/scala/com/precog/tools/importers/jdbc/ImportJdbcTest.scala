package com.precog.tools.importers.jdbc

import org.specs2.mutable.Specification
import blueeyes.json._
import Datatypes._
import ImportJdbc.{IngestInfo, ImportTable}
import akka.dispatch.Await
import akka.util.Duration
import blueeyes.core.data.DefaultBijections._
import blueeyes.akka_testing.FutureMatchers
import blueeyes.core.http.test.HttpRequestMatchers
import blueeyes.core.data._
import scala.Left
import com.precog.tools.importers.jdbc.ImportJdbc.IngestInfo
import scala.Right
import scala.Some
import com.precog.tools.importers.jdbc.ImportJdbc.ImportTable
import blueeyes.bkka.AkkaDefaults._
import blueeyes.core.http.{HttpStatus, HttpResponse}
import blueeyes.core.http.HttpStatusCodes.OK

/**
 * User: gabriel
 * Date: 11/22/12
 */
class ImportJdbcTest extends Specification with FutureMatchers with HttpRequestMatchers{

  "build queries" should {
  	"single table query" in {
  		ImportJdbc.buildQuery(tblADesc) must_== "select a.ID, a.name from A a order by a.ID, a.name"
  	}
  	"one to many query" in {
  		ImportJdbc.buildQuery(tblABDesc) must_==
      "select a.ID, a.name, b.ID, b.A_ID, b.name from A a left join B b on a.ID=b.A_ID order by a.ID, a.name, b.ID, b.A_ID, b.name"
  	}

    "many to many query" in {
      ImportJdbc.buildQuery(tblCABDesc) must_==
        "select c.A_ID, c.B_ID, c.name, a.ID, a.name, b.ID, b.A_ID, b.name "+
        "from C c left join A a on c.A_ID=a.ID left join B b on c.B_ID=b.ID " +
        "order by c.A_ID, c.B_ID, c.name, a.ID, a.name, b.ID, b.A_ID, b.name"
    }

    "circular query" in {
      ImportJdbc.buildQuery(tblDDesc) must_==
        "select dparent.ID, dparent.D_ID, dparent.name, dchild.ID, dchild.name " +
        "from D dparent left join D dchild on dparent.ID=dchild.D_ID " +
        "order by dparent.ID, dparent.D_ID, dparent.name, dchild.ID, dchild.name"
    }
  }

  "Json build from data" should {
    "build a simple Json" in {
      ImportJdbc.mkPartialJson("a",tblADesc,aData)._1 must_== jA
    }

    "build a composite Json" in {
      ImportJdbc.mkPartialJson("a",tblABDesc,aData++bData)._1 must_== jAB
    }

    "build a relation Json" in {
      ImportJdbc.mkPartialJson("c",tblCABDesc,cData++aData++bData)._1 must_== jC
    }

    "build a JArray for multiple values" in {
      val tblDesc = IngestInfo(List(ImportTable("parent",List("ID","name"), Left(Table("Parent"))),ImportTable("child",List("ID","name","P_ID"), Right(Join("id",Key(Table("child"),"parent_id"),ExportedKey)))))
      val dataChld1 = List("1","parent","1","child1","1")
      val dataNoChld = List("1","parent",null,null,null)
      val dataChld2 = List("1","parent","2","child2","1")
      val dataParent3 = List("3","parent3","2","child2","1")

      val (emptyChildJson,_)=ImportJdbc.mkPartialJson("parent",tblDesc,dataNoChld)
      emptyChildJson must_==
        JObject(JField("ID",JString("1"))::JField("name",JString("parent"))::JField("child",JArray(Nil))::Nil)


      val (partJson,m)=ImportJdbc.mkPartialJson("parent",tblDesc,dataChld2)
      partJson must_==
        JObject(JField("ID",JString("1"))::JField("name",JString("parent"))::
          JField("child",JArray(
            JObject(JField("ID",JString("2"))::JField("name",JString("child2"))::JField("P_ID",JString("1"))::Nil)::Nil)
          )::Nil)

      val (d1Json,m1)=ImportJdbc.mkPartialJson("parent",tblDesc,dataChld1,m)
      d1Json must_==
        JObject(JField("ID",JString("1"))::JField("name",JString("parent"))::
          JField("child",JArray(
            JObject(JField("ID",JString("1"))::JField("name",JString("child1"))::JField("P_ID",JString("1"))::Nil)::
              JObject(JField("ID",JString("2"))::JField("name",JString("child2"))::JField("P_ID",JString("1"))::Nil)::Nil)
          )::Nil)

      ImportJdbc.mkPartialJson("parent",tblDesc,dataParent3,m1)._1 must_== JObject(JField("ID",JString("3"))::JField("name",JString("parent3"))::
        JField("child",JArray(
          JObject(JField("ID",JString("2"))::JField("name",JString("child2"))::JField("P_ID",JString("1"))::Nil)::Nil)
        )::Nil)
    }
  }

  "Ingest data" should {

    implicit val executionContext = defaultFutureDispatch


    "ingest a single table" in new Conn{ val dbName ="single_ingest"
      tblA
      dataA
      val r=ImportJdbc.ingest(conn,"a",ImportJdbc.buildQuery(tblADesc),Some(tblADesc),basePath,host,apiKey)
      Await.result(r,1 minute) must beLike {
        case HttpResponse(_ ,_,Some(Left(buffer)),_) => { new String(buffer.array(), "UTF-8") must_== """{"failed":0,"skipped":0,"errors":[],"total":1,"ingested":1}"""}
      }
    }

    "ingest composite tables" in new Conn{ val dbName ="composite_ingest"
      tblA; tblB
      dataA; dataB
      cnstrBfkA

      val r=ImportJdbc.ingest(conn,"a",ImportJdbc.buildQuery(tblABDesc),Some(tblABDesc),basePath,host,apiKey)
      Await.result(r,1 minute) must beLike {
        case HttpResponse(_ ,_,Some(Left(buffer)),_) => { new String(buffer.array(), "UTF-8") must_== """{"failed":0,"skipped":0,"errors":[],"total":1,"ingested":1}"""}
        }
      }
    }
}

