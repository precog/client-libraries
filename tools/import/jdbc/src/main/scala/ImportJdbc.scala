import java.sql.{ Connection, DriverManager, ResultSet}
import dispatch._

//jdbc:mysql://localhost:3306/
//admin admin
object ImportJdbc {

  def main(args:Array[String])= args match {
    
		case Array(dbUrl,user,password) => importJdbc(dbUrl,user,password)
		case _ => error ("Incorrect number of parameters, required: database url user password")
	}

	def importJdbc(dbUrl:String, user:String, password:String) = {
		val conn= DriverManager.getConnection(dbUrl,user,password)
		val metadata= conn.getMetaData()
		println("Catalogs:")
		val catalogs = oneColumnRs(metadata.getCatalogs()).toArray;
		println(present(catalogs.toArray))
		println("Select a catalog")
		val cat=catalogs(readInt()-1)

		val tables = tablesList(metadata.getTables(cat,null,null,Array("TABLE"))).toArray;
		println("Tables:")
		println(present(tables.toArray))

		println("Select a table")
		val tabName=tables(readInt()-1)
		println("Table: %s".format(tabName))
		
		//use a prepared staemtent to get the metadata
		val stmt = conn.prepareStatement("select * form %s".format(tabName));
		val tblMetaData = stmt.getMetaData()
		val count=tblMetaData.getColumnCount();
        //column selection


        // use api key and dispatch to call ingest
        println("ingestion host")
        val host=readLine()
        println("API KEY for ingestion")
        val apiKey=readLine()
        println("ingestion path ( /{userId}/....)")
        val path=readLine()

        //consider lazyness
        val rs = stmt.executeQuery();
        val data= rsIterator(rs)( row => for (i <- 1 to count) yield row.getString(i))
        val body= data.map( _.mkString(",")).mkString("\n")
        val serverUrl= url("%s/ingest/v1/sync/fs/%s".format(host,path))
        var serverCall= serverUrl << body <<? Map("apiKey" -> apiKey) 
         
        

	}

	def rsIterator[T](rs:ResultSet)(f:ResultSet => T) = new Iterator[T] {
	  def hasNext = rs.next()
	  def next():T = f(rs)
	}

	def present[T](arr:Array[T])= (1 to arr.length).zip(arr).map(x=>x._1 +":"+ x._2).mkString(", ")

	def oneColumnRs(rs:ResultSet) = rsIterator(rs)(rs=> rs.getString(1))
	def tablesList(rs:ResultSet) = rsIterator(rs)(rs=> rs.getString("TABLE_NAME"))
	def tableDesc(rs:ResultSet) = rsIterator(rs)(rs=> (rs.getString("COLUMN_NAME"),rs.getString("TYPE_NAME")))
}



object Arm{
	def withConn(conn: Connection)( f:Connection=>Unit ){
		f(conn)
		conn.close()
	}
}


