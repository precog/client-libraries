import sbt._
import Keys._
import AltDependency._

object ScalaClientBuild extends Build {
  val buildOrganization = "com.reportgrid"
  val buildVersion = "0.3.1"
  val buildScalaVersion = "2.9.1"
  
  val blueeyes = GitAltDependency(_: java.io.File, file("../../blueeyes"),     RootProject(uri("git://github.com/reportgrid/blueeyes")))
  val rosetta =  GitAltDependency(_: java.io.File, file("../../RosettaJson"),  RootProject(uri("git://github.com/reportgrid/RosettaJson"))) 

  override def projectDefinitions(base: File) = {
    val client: Project = Project("scala-client", file(".")) settings(
      organization := buildOrganization,
      version      := buildVersion,
      scalaVersion := buildScalaVersion,
      scalacOptions ++= Seq("-deprecation", "-unchecked"),
      libraryDependencies ++= Seq(
        "org.apache.httpcomponents" %  "httpclient"          % "4.1.1",
        "net.databinder"            %% "dispatch-http-json"  % "0.8.5"   % "provided",
        "net.liftweb"               %% "lift-json"           % "2.4-M4"  % "provided" intransitive(),
        "org.specs2"                %% "specs2"              % "1.7-SNAPSHOT"  % "test"
      )
    ) dependsOnAlt(blueeyes(base)) dependsOnAlt(rosetta(base)) 
    
    client :: Nil
  }
}


// vim: set ts=4 sw=4 et:
