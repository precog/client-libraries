package com.precog.tools.importers.jdbc

import org.specs2.mutable.Specification
import blueeyes.json._
import Datatypes._
import akka.dispatch.{Future, Await}
import blueeyes.akka_testing.FutureMatchers
import blueeyes.core.http.test.HttpRequestMatchers
import scala.Left
import com.precog.tools.importers.jdbc.ImportJdbc.IngestInfo
import scala.Right
import scala.Some
import com.precog.tools.importers.jdbc.ImportJdbc.ImportTable
import blueeyes.bkka.AkkaDefaults._
import blueeyes.core.http.HttpResponse
import scalaz.{Id,StreamT}
import scalaz.effect.IO
import blueeyes.bkka.FutureMonad

/**
 * User: gabriel
 * Date: 11/22/12
 */
class ImportJdbcSpec extends Specification with FutureMatchers with HttpRequestMatchers{


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


  //implicit def toStreamElem[T](l:List[T])=l.toSeq::StreamT.empty

  "Json build from data" should {
    //(baseName:String, ingestInfo:IngestInfo, row: Seq[String], outStream:StreamT[M,JValue], currentObj:StrJVMap=Map())
    val empty=StreamT.empty[Id.Id,JValue]
    "build a simple Json" in {
      ImportJdbc.mkJson("a",tblADesc,aData,empty) must_==  (empty, Map("A" -> JObject(Map("ID" -> JString("1"), "name" -> JString("aaa")))))
    }

    "build a composite Json" in {
      ImportJdbc.mkJson("a",tblABDesc,aData++bData,empty) must_== (empty,
        Map(
          "A" -> JObject(Map("ID" -> JString("1"), "name" -> JString("aaa"))),
          "B" -> JArray(List(JObject(Map("ID" -> JString("2"), "A_ID" -> JString("1"), "name" -> JString("bbb")))))
        ))
    }

    "build a relation Json" in {
      ImportJdbc.mkJson("c",tblCABDesc, cData++aData++bData, empty) must_== ( empty,
        Map(
          "A" -> JObject(Map("ID" -> JString("1"), "name" -> JString("aaa"))),
          "B" -> JObject(Map("ID" -> JString("2"), "A_ID" -> JString("1"), "name" -> JString("bbb"))),
          "C" -> JObject(Map("A_ID" -> JString("1"), "B_ID" -> JString("2"), "name" -> JString("ccc")))
        ))
    }

    val tblDesc = IngestInfo(List(ImportTable("parent",List("ID","name"), Left(Table("Parent"))),ImportTable("child",List("ID","name","P_ID"), Right(Join("id",Key(Table("child"),"parent_id"),ExportedKey)))))
    val dataChld1 = List("1","parent","1","child1","1")
    val dataNoChld = List("1","parent",null,null,null)
    val dataChld2 = List("1","parent","2","child2","1")
    val dataParent3 = List("3","parent3","2","child2","1")



    "build Jobjects for multiple values" in {

      val(stream1,map1)=ImportJdbc.mkJson("parent",tblDesc,dataNoChld,StreamT.empty)
      map1 must_==
        Map("PARENT"->JObject(JField("ID",JString("1"))::JField("name",JString("parent"))::Nil),"CHILD"->JArray(Nil))


      val (stream2,map2)=ImportJdbc.mkJson("parent",tblDesc,dataChld2,stream1,map1)
      map2 must_==
      Map(
        "PARENT"-> JObject(JField("ID",JString("1"))::JField("name",JString("parent"))::Nil),
         "CHILD"-> JArray(
            JObject(JField("ID",JString("2"))::JField("name",JString("child2"))::JField("P_ID",JString("1"))::Nil)::Nil)
          )


      ImportJdbc.mkJson("parent",tblDesc,dataParent3,stream2,map2)._2 must_==
      Map(
        "PARENT"->JObject(JField("ID",JString("3"))::JField("name",JString("parent3"))::Nil),
        "CHILD"->JArray(
          JObject(JField("ID",JString("2"))::JField("name",JString("child2"))::JField("P_ID",JString("1"))::Nil)::Nil)
        )
    }

    "buildBody  for multiple values" in {
      val tblDesc = IngestInfo(List(ImportTable("parent",List("ID","name"), Left(Table("Parent"))),ImportTable("child",List("ID","name","P_ID"), Right(Join("id",Key(Table("child"),"parent_id"),ExportedKey)))))
      val dataChld1 = List("1","parent1","1","child1","1")
      val dataChld2 = List("1","parent1","2","child2","1")
      val dataNoChld = List("2","parent2",null,null,null)
      val dataParent3 = List("3","parent3","2","child2","1")

      //val allData= StreamT.fromIterable((dataChld1::dataChld2::dataNoChld::dataParent3::Nil).reverse.map( _.toIndexedSeq).toIterable)
      val allData= dataChld1::dataChld2::dataNoChld::dataParent3::StreamT.empty[IO,Seq[String]]

      implicit val executionContext = defaultFutureDispatch
      implicit val futureMonad= new FutureMonad(executionContext)

      val r= ImportJdbc.buildBody(allData,"parent",tblDesc)
      Await.result(r.flatMap(_.toStream),1 minute) must_==(
        JObject(JField("ID",JString("1"))::JField("name",JString("parent1"))::
          JField("CHILD",JArray(
            JObject(JField("ID",JString("1"))::JField("name",JString("child1"))::JField("P_ID",JString("1"))::Nil)::
            JObject(JField("ID",JString("2"))::JField("name",JString("child2"))::JField("P_ID",JString("1"))::Nil)::
            Nil)
          )::Nil)::
          JObject(JField("ID",JString("2"))::JField("name",JString("parent2"))::JField("CHILD",JArray(Nil))::Nil)::
          JObject(JField("ID",JString("3"))::JField("name",JString("parent3"))::
          JField("CHILD",JArray(
          JObject(JField("ID",JString("2"))::JField("name",JString("child2"))::JField("P_ID",JString("1"))::Nil)::Nil)
          )::Nil)::Nil).reverse
    }
  }


  "Ingest data" should {

    implicit val executionContext = defaultFutureDispatch


    "ingest a single table" in new Conn{ val dbName ="single_ingest"
      tblA
      dataA
      val r=ImportJdbc.ingest(conn,"a",ImportJdbc.buildQuery(tblADesc),Some(tblADesc),basePath,host,apiKey)
      Await.result(r,1 minute) must beLike {
        case HttpResponse(_ ,_,Some(Left(buffer)),_) => { new String(buffer.array(), "UTF-8") must_== """{"ingested":3,"errors":[]}"""}
      }
    }

    "ingest composite tables" in new Conn{ val dbName ="composite_ingest"
      tblA; tblB
      dataA; dataB
      cnstrBfkA

      val r=ImportJdbc.ingest(conn,"a",ImportJdbc.buildQuery(tblABDesc),Some(tblABDesc),basePath,host,apiKey)
      Await.result(r,1 minute) must beLike {
        case HttpResponse(_ ,_,Some(Left(buffer)),_) => { new String(buffer.array(), "UTF-8") must_== """{"ingested":3,"errors":[]}"""}
        }
      }
    }
}