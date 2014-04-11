import sbt._

object ImportToolsBuild extends Build {

  lazy val root = Project(id = "import-tools", base = file(".")) aggregate(common,jdbc, mongo)

  lazy val common = Project(id = "import-common", base = file("common"))

  lazy val mongo = Project(id = "import-mongodb", base = file("mongodb")) dependsOn("import-common")

  lazy val jdbc = Project(id = "import-jdbc", base = file("jdbc")) dependsOn("import-common")
}
