name := "serenity"

version := "0.0.1-SNAPSHOT"

scalaVersion := Versions.scalaVersion

libraryDependencies ++= Dependencies.dependencies

lazy val logic = project in file("modules/logic")

lazy val root = (project in file("."))
    .enablePlugins(PlayScala)
    .dependsOn(logic)
    .aggregate(logic)