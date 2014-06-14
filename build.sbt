name := """akka-eip"""

version := "1.0"

scalaVersion := "2.11.1"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.3"

// Tests
libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.0" % "test"

libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.3.3" % "test"
