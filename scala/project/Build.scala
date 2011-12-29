import sbt._
import Keys._

object ScalaClientBuild extends Build {
  val blueeyesDeps = com.samskivert.condep.Depends( 
    ("blueeyes",    null, "com.reportgrid" %% "blueeyes"     % "0.5.1")
  )

  val rosettaDeps =  com.samskivert.condep.Depends(
    ("RosettaJson", null, "com.reportgrid" %% "rosetta-json" % "0.3.5")
  )

  lazy val client: Project = ((blueeyesDeps.addDeps _) andThen (rosettaDeps.addDeps _)) {
    Project(id = "scala-client", base = file(".")).settings(
      organization := "com.reportgrid",
      version      := "0.3.2",
      scalaVersion := "2.9.1",
      scalacOptions ++= Seq("-deprecation", "-unchecked"),

      libraryDependencies ++= (blueeyesDeps.libDeps ++ rosettaDeps.libDeps ++ Seq(
        "org.apache.httpcomponents" %  "httpclient"          % "4.1.1",
        "net.databinder"            %% "dispatch-http-json"  % "0.8.5"   % "provided",
        "net.liftweb"               %% "lift-json"           % "2.4-M4"  % "provided" intransitive(),
        "org.specs2"                %% "specs2"              % "1.7-SNAPSHOT"  % "test")),
      
      resolvers ++= Seq("ReportGrid repo" at                   "http://nexus.reportgrid.com/content/repositories/releases",
                        "ReportGrid snapshot repo" at          "http://nexus.reportgrid.com/content/repositories/snapshots",
                        "ReportGrid public repo" at            "http://nexus.reportgrid.com/content/repositories/public-releases",
                        "ReportGrid public snapshot repo" at   "http://nexus.reportgrid.com/content/repositories/public-snapshots",
                        "Typesafe repo" at                     "http://repo.typesafe.com/typesafe/releases/"),

      publishTo <<= (version) { version: String =>
        val nexus = "http://nexus.reportgrid.com/content/repositories/"
        if (version.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus+"public-snapshots/") 
        else                                   Some("releases"  at nexus+"public-releases/")
      },

      credentials += Credentials(Path.userHome / ".ivy2" / ".rgcredentials")
    )
  }
}


// vim: set ts=4 sw=4 et:
