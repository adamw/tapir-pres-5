import sbt._
import Keys._
import com.softwaremill.SbtSoftwareMillCommon.commonSmlBuildSettings

name := "tapir-pres-5"
organization := "com.softwaremill"
scalaVersion := "3.2.2"

val tapirVersion = "1.4.0"

libraryDependencies ++= Seq(
  "com.softwaremill.sttp.tapir" %% "tapir-netty-server-id" % "0.1.1",
  "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion,
  "ch.qos.logback" % "logback-core" % "1.4.7",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
  "javax.ws.rs" % "javax.ws.rs-api" % "2.1.1",
  "javax.xml.bind" % "jaxb-api" % "2.3.1"
)

commonSmlBuildSettings
