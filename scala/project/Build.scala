import sbt._
import Keys._

object ScalaClientBuild extends Build {
  val deps = Seq(
    "org.apache.httpcomponents" %   "httpclient"          % "4.1.1",
    "net.databinder"            %%  "dispatch-http-json"  % "0.7.8"   % "provided",
    "net.liftweb"               %%  "lift-json"           % "2.3"     % "provided"
  )

  def tryLocalGit(buildBase: java.io.File, p: Project, f: java.io.File, git: URI): Project = {
    val resolved = if (f.isAbsolute) f else new File(buildBase, f.getPath)
    val dep = if(resolved.isDirectory) RootProject(resolved) else RootProject(git)
    p dependsOn dep
  }

  override def projectDefinitions(base: File) = tryLocalGit(base,
    Project("client", file(".")) settings(name := "scala-client", organization := "com.reportgrid", libraryDependencies ++= deps),
    file("../../rosetta-json"),
    uri("https://github.com/jdegoes/RosettaJson")
  ) :: Nil
}

