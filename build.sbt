import com.trueaccord.scalapb.{ScalaPbPlugin => PB}

name := "scaster"

version := "1.0"

scalaVersion := "2.11.5"

libraryDependencies ++= Seq(
  "io.spray" %% "spray-json" % "1.3.1",
  "javax.jmdns" % "jmdns" % "3.4.1",
  "org.slf4j" % "slf4j-api" % "1.7.10",
  "org.slf4j" % "log4j-over-slf4j" % "1.7.10",
  "ch.qos.logback" % "logback-classic" % "1.1.2",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
  "net.liftweb" % "lift-json_2.11" % "3.0-M3"
)

PB.protobufSettings
