package com.precog.tools.importers.jdbc

import org.specs2.mutable.Specification
import blueeyes.json.JsonAST.{JArray, JString, JField, JObject}
import com.precog.tools.importers.jdbc.ImportJdbc.{IngestInfo, ImportTable}

/**
 * User: gabriel
 * Date: 11/22/12
 */
class ImportJdbcTest extends Specification {

  "build queries" should {
    //buildQuery(base:Table, tblNames:List[String], columns:List[Seq[Column]], relations:List[Join])
  	"single table query" in {
  		ImportJdbc.buildQuery(tblADesc) must_== "select a.ID, a.name from A a order by a.ID, a.name"// order by 1,2"
  	}
  	"one to many query" in {
  		ImportJdbc.buildQuery(tblABDesc) must_==
      "select a.ID, a.name, b.ID, b.A_ID, b.name from A a left join B b on a.ID=b.A_ID order by a.ID, a.name, b.ID, b.A_ID, b.name"// order by 1,2,3,4,5"
  	}

    "many to many query" in {
      ImportJdbc.buildQuery(tblCABDesc) must_==
        "select c.A_ID, c.B_ID, c.name, a.ID, a.name, b.ID, b.A_ID, b.name "+
        "from C c left join A a on c.A_ID=a.ID left join B b on c.B_ID=b.ID " +
        "order by c.A_ID, c.B_ID, c.name, a.ID, a.name, b.ID, b.A_ID, b.name"// order by 1,2,3,4,5"
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
      val tblDesc = IngestInfo(List(ImportTable("parent",List("ID","name"), Left(Table("Parent"))),ImportTable("child",List("ID","name","P_ID"), Right(Join("id",Key(Table("child"),"parent_id"),exported)))))
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

    lazy val dbUrl="jdbc:mysql://localhost:3306/" //readLine("Enter database URL:")
    lazy val user="root" //"admin" //readLine("User:"))
    lazy val password = "root"//"admin" //readLine("Password:")
    // use api key and dispatch to call ingest
    lazy val host="http://beta.precog.com" //readLine("ingestion host")   //TODO move to trait ?
    lazy val apiKey="43AB865E-BB86-4F74-A57E-7E8BBD77F2B5"//readLine("API KEY for ingestion")
    lazy val basePath="/0000000457/data" //readLine("Base ingestion path ( /{userId}/....)")

    "ingest a single table" in new Conn("single_ingest"){
      tblA
      dataA
      ImportJdbc.ingest(conn,"a",ImportJdbc.buildQuery(tblADesc),Some(tblADesc),basePath,host,apiKey)

    }


  }


}

 

