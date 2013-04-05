name := "import-common"

organization := "org.precog"

version := "0.1"

scalaVersion := "2.9.2"

resolvers ++= Seq(
  "ReportGrid (public)" at "http://nexus.reportgrid.com/content/repositories/public-releases"
)

libraryDependencies ++= Seq(
  "com.reportgrid" %% "blueeyes-core" % "1.0.0-M8.1",
  "com.reportgrid" %% "blueeyes-json" % "1.0.0-M8.1"
  )


