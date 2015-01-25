name := """seinsrBackend"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  javaCore,
  javaJdbc,
  javaEbean,
  cache,
  javaWs,
  "junit" % "junit" % "4.8.1" % "test"
)



javacOptions ++= Seq("-Xlint:unchecked", "-Xlint:deprecation")

jacoco.settings