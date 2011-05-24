import sbt._

class ScalaClientProject(info: ProjectInfo) extends DefaultProject(info) with Repositories {
  val rosetta       = "github"                    %% "rosetta-json"        % "0.2"  
  val blueeyes      = "com.github.blueeyes"       %  "blueeyes"            % "0.3.18" 
  val http_client   = "org.apache.httpcomponents" %  "httpclient"   % "4.1.1"

  val specs         = "org.scala-tools.testing"   %% "specs"        % "1.6.7"
  val scala_check   = "org.scala-tools.testing"   %% "scalacheck"   % "1.8"   % "test"

  def scala_check_framework = new TestFramework("org.scalacheck.ScalaCheckFramework")

  override def testFrameworks = super.testFrameworks ++ Seq(scala_check_framework)
}

trait Repositories {
  val ScalaReleases   = MavenRepository("Scala Tools Releases",       "http://scala-tools.org/repo-releases/")
  val ScalaSnapshots  = MavenRepository("Scala Tools Snapshots",      "http://scala-tools.org/repo-snapshots/")
  val Sonatype        = MavenRepository("Sonatype Repository",        "http://oss.sonatype.org/content/repositories/releases/")
  val JBoss           = MavenRepository("JBoss Releases",             "http://repository.jboss.org/nexus/content/groups/public/")
  val Nexus           = MavenRepository("Nexus Scala Tools",          "http://nexus.scala-tools.org/content/repositories/releases/")
  val Maven           = MavenRepository("Maven Repo 1",               "http://repo1.maven.org/maven2/")
  val Scalable        = MavenRepository("Maven Repo 2",               "http://akka.io/repository/")
}
