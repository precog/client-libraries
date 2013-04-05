import sbt._
object JdbcImportProj extends Build
{
  lazy val root =
    Project("import-jdbc", file(".")) dependsOn(common)
  lazy val common =
    ProjectRef(uri("../common/"), "import-common")
}

