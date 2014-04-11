package com.precog.tools.importers

import java.sql.{DriverManager, Connection}
import jdbc.ImportJdbc.{ImportTable, IngestInfo}
import org.specs2.mutable.After
import org.specs2.specification.Scope
import blueeyes.json.{JArray, JString, JField, JObject}
import scala.Left
import scala.Right

/**
 * User: gabriel
 * Date: 12/4/12
 */
package object jdbc {
  import Datatypes._

  Class.forName("org.h2.Driver")

  // use api key and dispatch to call ingest
  val host="https://devapi.precog.com"
  val apiKey="0A24F09F-19CB-45D0-8BFA-543C61BA5EE6"
  val basePath="/0000000075/data"

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
  
  val relationAtoB= Set(Join(pkA.columnName,fkBtoA,ExportedKey))
  val relationBtoA= Set(Join(fkBtoA.columnName,pkA,ImportedKey))
  val relationsCtoAB= Set(Join(fkCtoA.columnName,pkA,ImportedKey),Join(fkCtoB.columnName,pkB,ImportedKey))
  val relationsAtoC= Set(Join(pkA.columnName,fkCtoA,ExportedKey))
  val relationsBtoC= Set(Join(pkB.columnName,fkCtoB,ExportedKey))

  val relationsDtoD= Set(Join(pkD.columnName,fkDtoD,ExportedKey),Join(fkDtoD.columnName,pkD,ImportedKey))

  val aCols= List("ID","name")
  val bCols= List("ID","A_ID","name")
  val cCols= List("A_ID","B_ID","name")
  val dCols= List("ID","name")

  val tblADesc =IngestInfo(List(ImportTable("a",aCols,Left(tA))))
  val tblABDesc=IngestInfo(List(ImportTable("a",aCols, Left(tA)),ImportTable("b",bCols, Right(Join(pkA.columnName,fkBtoA,ExportedKey)))))
  val tblCABDesc = IngestInfo(List(ImportTable("c",cCols, Left(tC)),ImportTable("a",aCols, Right(Join(fkCtoA.columnName,pkA,ImportedKey))),ImportTable("b",bCols, Right(Join(fkCtoB.columnName,pkB,ImportedKey)))))
  val tblDDesc=IngestInfo(List(ImportTable("dparent",List("ID","D_ID","name"),Left(tD)),ImportTable("dchild",List("ID","name"), Right(Join(pkD.columnName,fkDtoD,ExportedKey)))))

  val aData =List("1","aaa")
  val bData =List("2","1","bbb")
  val cData =List("1","2","ccc")

  val jA = JObject(JField("ID",JString("1"))::JField("name",JString("aaa"))::Nil)
  val jB =JObject(JField("ID",JString("2"))::JField("A_ID",JString("1"))::JField("name",JString("bbb"))::Nil)
  val jAB = JObject(JField("ID",JString("1"))::JField("name",JString("aaa"))::JField("B",JArray(jB::Nil))::Nil)
  val jC = JObject(JField("A_ID",JString("1"))::JField("B_ID",JString("2"))::JField("name",JString("ccc"))::JField("A",jA)::JField("B",JObject(JField("ID",JString("2"))::JField("A_ID",JString("1"))::JField("name",JString("bbb"))::Nil))::Nil)

  //def getConn(db:String)=DriverManager.getConnection("jdbc:h2:~/%s".format(db))

  trait Conn extends After with Scope {

    def dbName:String

    implicit lazy val conn= DriverManager.getConnection("jdbc:h2:~/%s".format(dbName))

    def after{
      conn.createStatement().execute(" drop all objects delete files ")
      conn.close()
    }
  }

}
