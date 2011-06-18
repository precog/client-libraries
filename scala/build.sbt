name := "reportgrid-client"

organization := "com.reportgrid"

version := "0.3.0"

scalaVersion := "2.9.0-1"

scalacOptions ++= Seq("-deprecation", "-unchecked")

libraryDependencies ++= Seq(
  "org.apache.httpcomponents" %  "httpclient"          % "4.1.1",
  "com.reportgrid"            %% "rosetta-json"        % "0.3.0",
  "com.reportgrid"            %% "blueeyes"            % "0.4.0"   % "provided",
  "net.databinder"            %% "dispatch-http-json"  % "0.8.3"   % "provided",
  "net.liftweb"               %% "lift-json"           % "2.4-M2"  % "provided"
)


