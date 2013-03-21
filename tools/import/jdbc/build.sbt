name := "import-jdbc"

version := "0.1"

scalaVersion := "2.9.2"

resolvers ++= Seq(
  "ReportGrid (public)" at "http://nexus.reportgrid.com/content/repositories/public-releases",
  "Sonatype" at "http://oss.sonatype.org/content/repositories/public",
  "Typesafe" at "http://repo.typesafe.com/typesafe/releases/",
  "Typesafe-snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
  "Scala Tools" at "http://scala-tools.org/repo-snapshots/",
  "JBoss"       at "http://repository.jboss.org/nexus/content/groups/public/",
  "Akka"        at "http://repo.akka.io/releases/",
  "GuiceyFruit" at "http://guiceyfruit.googlecode.com/svn/repo/releases/"
)

scalacOptions ++= Seq("-unchecked", "-deprecation")

assemblySettings

libraryDependencies ++= Seq(
  "com.reportgrid" % "blueeyes-core_2.9.2" % "1.0.0-M7.7",
  "com.reportgrid" % "blueeyes-json_2.9.2" % "1.0.0-M7.7",
  "org.specs2" %% "specs2" % "1.12.2" ,
  "com.h2database" % "h2" % "1.2.134" % "test"
)
