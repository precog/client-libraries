import sbt._
object MongoImportProj extends Build
{
  lazy val root =
    Project("import-mongo", file(".")) dependsOn(common)
  lazy val common =
    ProjectRef(uri("../common/"), "import-common")
}

