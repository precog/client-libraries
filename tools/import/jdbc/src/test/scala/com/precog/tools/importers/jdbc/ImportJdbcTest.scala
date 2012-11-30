package com.precog.tools.importers.jdbc

import org.specs2.specification._
import org.specs2.mutable.{Specification, After }
import java.sql.{Connection, DatabaseMetaData, DriverManager}
import DbAccess._
import DbAnalysis._
import blueeyes.json.JsonAST.{JArray, JString, JField, JObject}


//import ImportJdbc

/**
 * User: gabriel
 * Date: 11/22/12
 */
class ImportJdbcTest extends Specification {

  Class.forName("org.h2.Driver") 


  def tblA(implicit conn:Connection) = conn.createStatement().execute(" create table A( id int primary key, name varchar(10) ) ") 
  def tblB(implicit conn:Connection) = conn.createStatement().execute(" create table B( id int primary key, a_id int, name varchar(10)) ")
  def cnstrBfkA(implicit conn:Connection) = conn.createStatement().execute(" alter table B add constraint ab_fk foreign key (a_id) references A")

  def tblC(implicit conn:Connection) = conn.createStatement().execute(" create table C( a_id int, b_id int, name varchar(10)) ")
  def cnstrCfkA(implicit conn:Connection)= conn.createStatement().execute(" alter table C add constraint ac_fk foreign key (a_id) references A")
  def cnstrCfkB(implicit conn:Connection)= conn.createStatement().execute(" alter table C add constraint bc_fk foreign key (b_id) references B")

  def tblD(implicit conn:Connection) = conn.createStatement().execute(" create table D( id int primary key, d_id int, name varchar(10)) ")
  def cnstrDfkD(implicit conn:Connection) = conn.createStatement().execute(" alter table D add constraint d_fk foreign key (d_id) references D")

  def dataA(implicit conn:Connection)=conn.createStatement().execute(" insert into A values (1,'a1'),(2,'a2'),(3,'a3') ")
  def dataB(implicit conn:Connection)=conn.createStatement().execute(" insert into B values (1,1,'b1'),(2,1,'b2'),(3,3,'b3') ")
  def dataBnoA(implicit conn:Connection)=conn.createStatement().execute(" insert into B values (1,null,'b1'),(2,null,'b2'),(3,null,'b3') ")
  def dataC(implicit conn:Connection)=conn.createStatement().execute(" insert into C values (1,1,'a1b1'),(2,1,'a2b1'),(2,2,'a2b2'),(3,3,'a3b3') ")
  def dataD(implicit conn:Connection)=conn.createStatement().execute(" insert into D values (1,null,'d1'),(2,1,'d2d1'),(3,2,'d3d2') ")

  val tA = Table("A")
  val tB = Table("B")
  val pkA = Key(tA,"ID")
  val pkB = Key(tB,"ID")
  val fkBtoA = Key(tB,"A_ID")

  val tC = Table("C")
  val fkCtoA = Key(tC,"A_ID")
  val fkCtoB = Key(tC,"B_ID")

  val tD = Table("D")
  val pkD = Key(tD,"ID")
  val fkDtoD = Key(tD,"D_ID")

  val relationAtoB= Set(Join(pkA.columnName,fkBtoA,true))
  val relationBtoA= Set(Join(fkBtoA.columnName,pkA,false))
  val relationsCtoAB= Set(Join(fkCtoA.columnName,pkA,false),Join(fkCtoB.columnName,pkB,false))
  val relationsAtoC= Set(Join(pkA.columnName,fkCtoA,true))
  val relationsBtoC= Set(Join(pkB.columnName,fkCtoB,true))

  val relationsDtoD= Set(Join(pkD.columnName,fkDtoD,true),Join(fkDtoD.columnName,pkD,false))//Set(Join(pkD.columnName,fkDtoD),Join(fkDtoD.columnName,pkD))

  def getConn(db:String)=DriverManager.getConnection("jdbc:h2:~/%s".format(db))
  def cleanUp(conn:Connection) {
  	conn.createStatement().execute(" drop all objects delete files ")
    conn.close()
  }

  "declared relations" should {
  	"identify one to many" in new Conn("onemany"){ 

	  tblA;  tblB
	  cnstrBfkA

	  val metadata= conn.getMetaData
	  getDeclaredRelationships(metadata,null,tA) must_== relationAtoB
	  getDeclaredRelationships(metadata,null,tB) must_== relationBtoA
  	}

  	"identify many to many" in new Conn("manymany") { 

	  tblA; tblB; tblC
	  cnstrCfkA; cnstrCfkB

	  val metadata= conn.getMetaData
	  getDeclaredRelationships(metadata,null,tC) must_== relationsCtoAB
	  getDeclaredRelationships(metadata,null,tA) must_== relationsAtoC
	  getDeclaredRelationships(metadata,null,tB) must_== relationsBtoC
  	}

  	"identify circular" in new Conn("self") { 

	  tblD
	  cnstrDfkD

	  val metadata= conn.getMetaData
	  getDeclaredRelationships(metadata,null,tD) must_== relationsDtoD
  	}
  }



  "inferred relations" should {
  	"identify one to many" in new Conn("ionemany"){ 

	  tblA;  tblB
	  cnstrBfkA

	  dataA; dataB

    val metadata= conn.getMetaData
	  getInferredRelationships(conn,metadata,null,tA, "ID") must_== relationAtoB
	  getInferredRelationships(conn,metadata,null,tB, "ID") must_== relationBtoA
  	}

  	"identify many to many" in new Conn("imanymany") { 

	  tblA; tblB; tblC
	  cnstrCfkA; cnstrCfkB

	  dataA; dataBnoA; dataC

	  val metadata= conn.getMetaData
	  getInferredRelationships(conn,metadata,null,tC, "ID") must_==(relationsCtoAB)
	  getInferredRelationships(conn,metadata,null,tA, "ID") must_==(relationsAtoC)
	  getInferredRelationships(conn,metadata,null,tB, "ID") must_==(relationsBtoC)
  	}

  	"identify circular" in new Conn("iself") { 

	  tblD
	  cnstrDfkD
	  dataD

	  val metadata= conn.getMetaData
	  getInferredRelationships(conn,metadata,null,tD, "ID") must_== Set(Join(pkD.columnName,fkDtoD,true)) // can't infer both ways as relationsDtoD
  	}
  }

  val aCols= List("ID","name")
  val bCols= List("ID","A_ID","name")
  val cCols= List("A_ID","B_ID","name")
  val dCols= List("ID","name")

  val tblADesc =List(("a",aCols, false))
  val tblABDesc=List(("a",aCols, false),("b",bCols, false))
  val tblCABDesc = List(("c",cCols, false),("a",aCols, false),("b",bCols, false))
  val tblDDesc=List(("dparent",List("ID","D_ID","name"),false),("dchild",List("ID","name"), true))

  "build queries" should {
    //buildQuery(base:Table, tblNames:List[String], columns:List[Seq[Column]], relations:List[Join])
  	"single table query" in {
  		ImportJdbc.buildQuery(tA,tblADesc,List()) must_== "select a.ID, a.name from A a order by a.ID, a.name"// order by 1,2"
  	}
  	"one to many query" in {
  		ImportJdbc.buildQuery(tA,tblABDesc,relationAtoB.toList) must_==
      "select a.ID, a.name, b.ID, b.A_ID, b.name from A a left join B b on a.ID=b.A_ID order by a.ID, a.name, b.ID, b.A_ID, b.name"// order by 1,2,3,4,5"
  	}

    "many to many query" in {
      ImportJdbc.buildQuery(tC,tblCABDesc,relationsCtoAB.toList) must_==
        "select c.A_ID, c.B_ID, c.name, a.ID, a.name, b.ID, b.A_ID, b.name "+
        "from C c left join A a on c.A_ID=a.ID left join B b on c.B_ID=b.ID " +
        "order by c.A_ID, c.B_ID, c.name, a.ID, a.name, b.ID, b.A_ID, b.name"// order by 1,2,3,4,5"
    }

    "circular query" in {
      ImportJdbc.buildQuery(tD,tblDDesc, relationsDtoD.toList) must_==
        "select dparent.ID, dparent.D_ID, dparent.name, dchild.ID, dchild.name " +
        "from D dparent left join D dchild on dparent.ID=dchild.D_ID " +
        "order by dparent.ID, dparent.D_ID, dparent.name, dchild.ID, dchild.name"
    }
  }

  val aData =List("1","aaa")
  val bData =List("2","1","bbb")
  val cData =List("1","2","ccc")

  val jA = JObject(JField("ID",JString("1"))::JField("name",JString("aaa"))::Nil)
  val jB =JObject(JField("ID",JString("2"))::JField("A_ID",JString("1"))::JField("name",JString("bbb"))::Nil)
  val jAB = JObject(JField("ID",JString("1"))::JField("name",JString("aaa"))::JField("b",jB)::Nil)
  val jC = JObject(JField("A_ID",JString("1"))::JField("B_ID",JString("2"))::JField("name",JString("ccc"))::JField("a",jA)::JField("b",JObject(JField("ID",JString("2"))::JField("A_ID",JString("1"))::JField("name",JString("bbb"))::Nil))::Nil)

  "Json build from data" should {
    "build a simple Json" in {
      ImportJdbc.mkPartialJson("a",tblADesc,aData)._1 must_== jA
    }

    "build a composite Json" in {
      ImportJdbc.mkPartialJson("b",tblABDesc,aData++bData)._1 must_== jB
    }

    "build a relation Json" in {
      ImportJdbc.mkPartialJson("c",tblCABDesc,cData++aData++bData)._1 must_== jC
    }

    "build a JArray for multiple values" in {
      val tblDesc = List(("parent",List("ID","name"), false),("child",List("ID","name","P_ID"), true))
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


  case class Conn(s:String) extends After with Scope {

  	implicit lazy val conn= DriverManager.getConnection("jdbc:h2:~/%s".format(s))

    def after{ cleanUp(conn) }
  }
}

 

