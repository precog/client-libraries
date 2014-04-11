package com.precog.tools.importers.mongo

import com.mongodb.casbah.Imports._
import blueeyes.persistence.mongo.RealMongoSpecSupport
import com.mongodb.casbah.MongoDB
import org.specs2.mutable.After
import org.specs2.specification.Scope


/**
 * User: gabriel
 * Date: 3/29/13
 */
class ImportMongoSpec extends RealMongoSpecSupport  {

  trait Mongo extends After with Scope {

    def dbName:String

    implicit lazy val testDb= MongoDB(realMongo, dbName )

    def after{
      testDb.dropDatabase()
    }
  }


  //def readFromMongo(mongoDB: MongoDB, collName: String, idCol:String, oLastId:Option[AnyRef], fieldNames:Seq[String]):Stream[DBObject]={
  "read from mongo" should  {
    "return only selected columns" in new Mongo {
      val dbName="t1"
      val newObj = MongoDBObject("a" -> "1", "x" -> "y", "b" -> 3, "spam" -> "eggs")
      testDb("test1").save(newObj)
      val r=ImportMongo.readFromMongo(testDb,"test1","_id", Seq("a","b"))
      r.head must_== MongoDBObject("_id"->newObj("_id"),"a" -> "1",  "b" -> 3)
    }

    "return the whole connection if no last id" in new Mongo {
      val dbName="t2"
      val data = List(MongoDBObject("a" -> 1),MongoDBObject("a" -> 2),MongoDBObject("a" -> 3),MongoDBObject("a" -> 4))
      data.foreach( testDb("test2").save(_) )
      val r=ImportMongo.readFromMongo(testDb,"test2","a",Seq("a"))
      r must containTheSameElementsAs(data)
    }

    "return only new rows" in new Mongo{
      val dbName="t3"
      val (d1,d2,d3,d4)=(MongoDBObject("a" -> 1),MongoDBObject("a" -> 2),MongoDBObject("a" -> 3),MongoDBObject("a" -> 4))
      val data = List(d1,d2,d3,d4)
      data.foreach( testDb("test3").save(_) )
      ImportMongo.readFromMongo(testDb,"test3","a", Seq("a")) must containTheSameElementsAs(data)
      val r=ImportMongo.readFromMongo(testDb,"test3","a", Seq("a"),Some(2))
      r must containTheSameElementsAs(List(d3,d4))
    }

    "return empty if no new rows" in new Mongo{
      val dbName="t4"
      val (d1,d2,d3,d4)=(MongoDBObject("a" -> 1),MongoDBObject("a" -> 2),MongoDBObject("a" -> 3),MongoDBObject("a" -> 4))
      val data = List(d1,d2,d3,d4)
      data.foreach( testDb("test4").save(_) )
      val r=ImportMongo.readFromMongo(testDb,"test4","a", Seq("a"),Some(4))
      r must be empty
    }
  }

  "columns of" should {
    "return no columns for the empty object" in {
      ImportMongo.columnsOf(MongoDBObject()) must be empty
    }

    "return the set of columns of an object" in {
      ImportMongo.columnsOf(MongoDBObject("a"->1,"c"->3, "column"->"zzzz")) must containTheSameElementsAs(Seq("c","column","a"))
    }
  }


  "sample columns" should {
    "return no columns when the collection is empty" in new Mongo{
      val dbName="t5"
      val data = List()
      data.foreach( testDb("test5").save(_) )
      val conn=new MongoConnection(realMongo)
      val cols=ImportMongo.sampleColumns(conn)(testDb,"test5")
      cols must be empty
    }
    "identify all the columns with a collection smaller than the sample size" in new Mongo{
      val dbName="t6"
      val data = List(MongoDBObject("a" -> 1,"b"->"a"),MongoDBObject("a" -> 2,"b"->"b"),MongoDBObject("a" -> 3,"b"->"c"),MongoDBObject("a" -> 4,"b"->"d"))
      data.foreach( testDb("test6").save(_) )

      val conn=new MongoConnection(realMongo)
      val cols=ImportMongo.sampleColumns(conn)(testDb,"test6")
      cols must_== Set("_id","a","b")
    }

    "identify all the columns with a collection bigger than the sample size" in new Mongo{
      val dbName="t7"
      (1 to 2*ImportMongo.sampleSize).foreach (i=>{testDb("test7").save(MongoDBObject("data" -> "asdb","idx"->i))})


      val conn=new MongoConnection(realMongo)
      val cols=ImportMongo.sampleColumns(conn)(testDb,"test7")
      cols must_== Set("_id","data","idx")
    }

    "identify all the columns for collections with different objects" in new Mongo{
      val dbName="t8"
      val data = List(MongoDBObject("a" -> 1,"b"->"a"),MongoDBObject("c" -> 2,"d"->"b"),MongoDBObject("a" -> 3,"b"->"c","z"->123),MongoDBObject("a" -> 4,"b"->"d"))
      data.foreach( testDb("test8").save(_) )

      val conn=new MongoConnection(realMongo)
      val cols=ImportMongo.sampleColumns(conn)(testDb,"test8")
      cols must_== Set("_id","a","b","c","d","z")
    }
  }

}
