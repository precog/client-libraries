import annotation.tailrec
import java.sql._
import dispatch._
import scala.Array


object ImportJdbc {

  def main(args:Array[String])= importJdbc()

  def buildUnion(r: Relation): String = {
    val pkTable=r.pk.table.name
    val fkTable=r.fk.table.name
    " join %s on %s.%s=%s.%s ".format(fkTable,pkTable,r.pk.columnName,fkTable,r.fk.columnName)
  }

  def buildJoins(tables:Set[Table],relations: Set[Relation]) =
    if (relations.isEmpty) tables.head.name
    else relations.foldLeft(relations.head.pk.table.name)( (q,r)=> q + buildUnion(r) ) //doesn't properly support composite keys yet


  def mkJson(s: Seq[(String, String)])= "{ %s }".format( s.map( lv => "\"%s\":\"%s\"".format(lv._1,lv._2) ).mkString(","))

  def buildQuery(columns:String, joins:String) = "select %s from %s".format(columns,joins)

  def importJdbc() {

    //TODO add support for cross catalogs/schemas
    println("Welcome to Precog JDBC import wizard")
    val dbUrl=readLine("Enter database URL:")
    val user= readLine("User:")
    val password = readLine("Password:")
    val conn= DriverManager.getConnection(dbUrl,user,password)
    val metadata= conn.getMetaData
    println("Catalogs:")
    val catalogs = oneColumnRs(metadata.getCatalogs).toArray
    println(present(catalogs))
    print("Select a catalog:")
    val cat=catalogs(readInt()-1)

    //for querying tables, the connection must be specific to a database
    val connDb= DriverManager.getConnection("%s%s".format(dbUrl,cat),user,password)
    val queries =
      if (readLine("Do you have a SQL query to select the data? (y/N)").toLowerCase == "y") {
        Set(readLine("Query="))
      } else createQueries(connDb,metadata, cat, selectedTables(findTables(metadata, cat, readTableName())))

    // use api key and dispatch to call ingest
    val host=readLine("ingestion host")
    val apiKey=readLine("API KEY for ingestion")
    val basePath=readLine("Base ingestion path ( /{userId}/....)")

    queries.map( join => {

      val starQuery= buildQuery("*",join)
      val labels = getColumns(connDb,starQuery)

      //column selection
      val selectedCols= selectSet("column",labels.toSet, Set())

      val path="%s/%s".format(basePath,readLine("table path %s/".format(basePath)))

      val columnQuery=buildQuery(selectedCols.mkString(","),join)
      //consider lazyness to build the body, a huge dataset can give out of memory although the ResultSet is already an iterator
      val stmt = connDb.prepareStatement(columnQuery)
      val count=columnCount(stmt)
      val rs = stmt.executeQuery()

      val data= rsIterator(rs)( row => for (i <- 1 to count) yield row.getString(i))
      val body=  "%s".format(data.map( row => mkJson(labels.zip(row))).mkString("\n"))
      val fullPath ="%s/ingest/v1/sync/fs%s/".format(host,path)
      val serverUrl= url(fullPath)
      println(fullPath)
      println(body)
      val serverCall= serverUrl << body <<? Map("apiKey" -> apiKey)
      val response = Http(serverCall OK as.String)
      println(response())
    })
  }


  def getDeclaredRelationships(metadata:DatabaseMetaData, cat:String, table: Table) =
    relationshipDesc(metadata.getExportedKeys(cat, null, table.name)).toSet ++
      relationshipDesc(metadata.getImportedKeys(cat, null, table.name)).toSet

  def isSelected(s: Set[Table], r: Relation) = s.contains(r.pk.table) && s.contains(r.fk.table)
  def filterSelected(relations: Set[Relation], selectedTables: Set[Table]) = relations.filter(r => isSelected(selectedTables, r))

  def createQueries(conn:Connection, metadata: DatabaseMetaData, cat: String, selected: Set[Table]): Set[String] = {
    val declaredRelationships: Set[Relation] = selected.flatMap(getDeclaredRelationships(metadata,cat,_))
    val allRelationships = relationships(declaredRelationships, conn, metadata, cat)
    //use only relationships between tables we already selected
    val relatedSelected = filterSelected(allRelationships, selected)
    val partitions =buildPartitions(selected, relatedSelected)
    val ps=partitions.map(p => buildJoins(p,filterSelected(relatedSelected, p)))
    println(ps.mkString("\n"))
    ps
  }


  def relationships(declaredRelationships: Set[Relation], conn: Connection, metadata: DatabaseMetaData, cat: String): Set[Relation] = {

    println("Declared relationsips found:")
    println(declaredRelationships)
    if (readLine("Infer non-declared relationships? (y/n)").toLowerCase == "y") {
      val idPattern = readLine("Id pattern? (e.g. '%%_id' )")
      val inferredRelationships: Set[Relation] = inferReferencedTables(conn, metadata, cat, idPattern)
      show(inferredRelationships)
      declaredRelationships.union(inferredRelationships)
    } else {
      declaredRelationships
    }

  }

  def buildPartitions(selected: => Set[Table], relatedSelected: Set[Relation]): Set[Set[Table]] = {
    if (readLine("Denormalize related tables? (y/n)").toLowerCase == "y") {
      //for denormalization, instead of pulling one table at a time, we pull related tables in one query
      //so we group selected tables based on the relationships between them
      minimizePartitions(selected.map(Set(_)).toSet, relatedSelected.map(r => (r.pk.table, r.fk.table)).toList)
    } else {
      selected.map(Set(_))
    }
  }

  def findTables(metadata: DatabaseMetaData, cat: String, tableName: => String): Array[Table] = {
    tables(metadata.getTables(cat, null, tableName, Array("TABLE"))).toArray
  }

  def selectedTables(tablesList: Array[Table]): Set[Table] = {
    println(present(tablesList))
    selectSet("table", tablesList.toSet, Set())
  }

  def readTableName()= {
    val tableName = readLine("Enter table name (blank to show all tables, %% for wildcard )")
    if (tableName == "") null else tableName
  }

  def findBaseTable(idPattern:String, name:String, colList:List[Column]):Option[Column]={
    colList.find(c => c.table == name+idPattern)
  }

  def countMatches(conn:Connection, baseCol: Column, refCol: Column)=
    conn.createStatement().executeQuery(" select count(*) from %s inner join %s using (%s)".format(baseCol.table,refCol.table,baseCol.name)).getInt(1) //might be better with a "where exists"


  def inferReferencedTables(conn:Connection, metadata:DatabaseMetaData, catalog:String, idPattern:String):Set[Relation]={
    val columnList:List[Column]=columns(metadata.getColumns(catalog,null,null,idPattern)).toList
    val similar:Map[String,List[Column]]=columnList.groupBy(_.name)

    for (
      (name, colList) <- similar.toSet;
      baseTblCol <- findBaseTable(idPattern,name,colList).toList;
      column <- colList if (column.table != baseTblCol.table && countMatches(conn,baseTblCol,column)>0)
    ) yield Relation(Key(Table(baseTblCol.table),baseTblCol.name),Key(Table(column.table),column.name),1)

  }

  def columnCount(stmt:PreparedStatement)=stmt.getMetaData.getColumnCount

  def getColumns(conn:Connection, query:String)={
    //use a prepared statement to get the metadata
    //might have to revert back to query & result set
    val stmt = conn.prepareStatement(query)
    val tblMetaData = stmt.getMetaData
    val count=columnCount(stmt)
    for ( i <- 1 to count) yield ( tblMetaData.getColumnName(i))
  }

  def rsIterator[T](rs:ResultSet)(f:ResultSet => T) = new Iterator[T] {
    def hasNext = rs.next()
    def next():T = f(rs)
  }

  def present[T](arr:Seq[T])= (1 to arr.length).zip(arr).map(x=>x._1 +":"+ x._2).mkString(", ")
  def show(set: Set[Relation])= set.map( r=> "%s with %s on %s=%s".format( r.pk.table,r.fk.table, r.pk.columnName,r.fk.columnName )).mkString(", ")

  def oneColumnRs(rs:ResultSet) = rsIterator(rs)(rs=> rs.getString(1))
  def tables(rs:ResultSet) = rsIterator(rs)(rs=> Table(rs.getString("TABLE_NAME")))
  def columns(rs:ResultSet) = rsIterator(rs)(rs=> Column(rs.getString("COLUMN_NAME"),rs.getString("TYPE_NAME"), rs.getString("TABLE_NAME")))
  def relationshipDesc(rs:ResultSet) = rsIterator(rs)(
      rs=> Relation(
        Key(Table(rs.getString("PKTABLE_NAME")),rs.getString("PKCOLUMN_NAME")),
        Key(Table(rs.getString("FKTABLE_NAME")),rs.getString("FKCOLUMN_NAME")),
        rs.getInt("KEY_SEQ")
      )
    )

  def allSet(rs:ResultSet) = {
    val count= rs.getMetaData.getColumnCount
    rsIterator(rs)(rs=> for ( i <- 1 to count) yield rs.getString(i) )
  }

  @tailrec
  def selectSet[T](label:String, available: Set[T], selected: Set[T])(implicit arg0: ClassManifest[T]): Set[T] =
    if (available.isEmpty) selected
    else {
      val availArray=available.toArray

      println("Available %ss:".format(label))
      println(present(availArray))

      println("Selected %ss:".format(label))
      println(present(selected.toList))

      println("Select a %s, 0 to select all, or enter to continue:".format(label))
      val selIdx = readLine()
      selIdx match {
        case "" => selected
        case ParseInt(0) => available
        case ParseInt(x) if (x<=available.size) => {
          val tabName = availArray(x - 1)
          selectSet(label,available-tabName, selected+tabName)
        }
        case _ => selectSet(label,available, selected)
      }
    }

  object ParseInt{
    def unapply(s : String) : Option[Int] = try {
      Some(s.toInt)
    } catch {
      case _ : java.lang.NumberFormatException => None
    }
  }

  def minimizePartitions[T](p:Set[Set[T]],relations:List[(T,T)]):Set[Set[T]]=
    relations.foldLeft(p)( (part,r) => {
        val (e1,e2) = r
        val newPart=for (
          s1 <- part.find(_.contains(e1));
          s2 <- part.find(_.contains(e2))
          if (s1 != s2)
        ) yield part-s1-s2+s1.union(s2)
        newPart.getOrElse(part)
      }
    )
}

case class Table(name:String){ override val toString=name }
case class Column(name:String, typeName:String,table:String){ override val toString="%s.%s:%s".format(table,name,typeName) }
case class Key(table:Table, columnName:String){ override val toString="%s.%s".format(table,columnName) }
case class Relation(pk:Key,fk:Key, seq:Int){ override val toString="%s(pk)->%s(fk) (%s)".format(pk,fk,seq) }
