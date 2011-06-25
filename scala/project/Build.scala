import sbt._
import Keys._
import AltDependency._

object ScalaClientBuild extends Build {
  val buildOrganization = "com.reportgrid"
  val buildVersion = "0.3.0"
  val buildScalaVersion = "2.9.0-1"
  
  val blueeyes = GitAltDependency(_: java.io.File, file("../../blueeyes"),     RootProject(uri("git://github.com/jdegoes/blueeyes")))
  val rosetta =  GitAltDependency(_: java.io.File, file("../../rosetta-json"), RootProject(uri("git://github.com/jdegoes/RosettaJson"))) 

  override def projectDefinitions(base: File) = {
    val client: Project = Project("scala-client", file(".")) settings(
      organization := buildOrganization,
      version      := buildVersion,
      scalaVersion := buildScalaVersion,
      scalacOptions ++= Seq("-deprecation", "-unchecked"),
      libraryDependencies ++= Seq(
        "org.apache.httpcomponents" %  "httpclient"          % "4.1.1",
        "net.databinder"            %% "dispatch-http-json"  % "0.8.3"   % "provided",
        "net.liftweb"               %% "lift-json"           % "2.4-M2"  % "provided",
        "org.scala-tools.testing"   %% "specs"               % "1.6.8"   % "test"
      )
    ) dependsOnAlt(blueeyes(base)) dependsOnAlt(rosetta(base)) 
    
    client :: Nil
  }
}


// vim: set ts=4 sw=4 et:
