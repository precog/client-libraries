name := "import-jdbc"

version := "0.1"

scalaVersion := "2.9.1"

resolvers ++= Seq(
  "Sonatype" at "http://oss.sonatype.org/content/repositories/public",
  "Typesafe" at "http://repo.typesafe.com/typesafe/releases/",
  "Typesafe-snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
  "Scala Tools" at "http://scala-tools.org/repo-snapshots/",
  "JBoss"       at "http://repository.jboss.org/nexus/content/groups/public/",
  "Akka"        at "http://repo.akka.io/releases/",
  "GuiceyFruit" at "http://guiceyfruit.googlecode.com/svn/repo/releases/"
)


libraryDependencies ++= Seq(
  "com.github.jdegoes" % "blueeyes-core_2.9.1" % "0.6.1-SNAPSHOT" % "compile",
  "com.github.jdegoes" % "blueeyes-json_2.9.1" % "0.6.1-SNAPSHOT" % "compile",
  "org.specs2" %% "specs2" % "1.12.3" % "test",
  "com.h2database" % "h2" % "1.2.134" 
)
