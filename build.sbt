name := """play-twitter-oauth-sample"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws,
  "org.scalatra.scalate" %% "scalate-core" % "1.7.0",
  "org.twitter4j" % "twitter4j-core" % "4.0.2"
)
