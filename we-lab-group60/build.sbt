name := """we-lab-group60"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  javaJdbc,
  javaCore,
  javaJpa,
  "org.hibernate" % "hibernate-entitymanager" % "4.3.1.Final",
  "com.google.code.gson" % "gson" % "2.2",
  cache
)     

TwirlKeys.templateImports += "scala.collection._" 

TwirlKeys.templateImports += "at.ac.tuwien.big.we15.lab2.api._"





