import java.sql.{ Connection, DriverManager, ResultSet}

//jdbc:mysql://localhost:3306/
//admin admin
object ImportJdbc {

  Class.forName("com.mysql.jdbc.Driver")

  def main(args:Array[String])= args match {
    
		case Array(url,user,password) => importJdbc(url,user,password)
		case _ => error ("Incorrect number of parameters, required: url user password")
	}

	def importJdbc(url:String, user:String, password:String) = {
		val conn= DriverManager.getConnection(url,user,password)
		val metadata= conn.getMetaData()
		println("Catalogs:")
		val catalogs = oneColumnRs(metadata.getCatalogs()).toArray;
		println(present(catalogs.toArray))
		println("Select a catalog")
		val cat=catalogs(readInt-1)
		val tables = tablesList(metadata.getTables(cat,null,null,null)).toArray;
		println("Tables:")
		println(present(tables.toArray))
		println("Select a table")
		val tab=tables(readInt-1)
		println("Table: %s".format(tab))
		val table=tableDesc(metadata.getColumns(cat,null,tab,null))
		println(table.mkString(" | "))
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


