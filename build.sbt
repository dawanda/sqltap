import AssemblyKeys._

name := "SQLTap"

organization := "com.paulasmuth"

version := "0.7.19"

mainClass in (Compile, run) := Some("com.paulasmuth.sqltap.SQLTap")

scalaSource in Compile <<= baseDirectory(_ / "src")

scalaVersion := "2.11.4"

assemblySettings

jarName in assembly := { s"${name.value.toLowerCase}-${version.value}.jar" }

fork in run := true

libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "1.0.2"
