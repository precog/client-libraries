import AssemblyKeys._ 

name := "import-mongodb"

organization := "org.precog"

version := "0.2"

scalaVersion := "2.9.2"

resolvers ++= Seq(
  "ReportGrid (public)" at "http://nexus.reportgrid.com/content/repositories/public-releases",
  "Sonatype" at "http://oss.sonatype.org/content/repositories/public",
  "Typesafe" at "http://repo.typesafe.com/typesafe/releases/",
  "Typesafe-snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
  "Scala Tools" at "http://scala-tools.org/repo-snapshots/"
)

scalacOptions ++= Seq("-unchecked", "-deprecation")

assemblySettings

libraryDependencies ++= Seq(
  "com.reportgrid" %% "blueeyes-core" % "1.0.0-M7.7",
  "com.reportgrid" %% "blueeyes-json" % "1.0.0-M7.7",
  "com.reportgrid" %% "blueeyes-mongo" % "1.0.0-M7.7",
  "org.mongodb" %% "casbah" % "2.3.0",
  "org.scalaz"  %% "scalaz-core"        % "7.0.0-M3" ,
  "org.specs2" %% "specs2" % "1.12.2" % "test"
)
