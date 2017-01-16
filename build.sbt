name := "serenity"

version := "0.0.1-SNAPSHOT"

scalaVersion := Versions.scalaVersion

libraryDependencies ++= Dependencies.playDependencies

lazy val logic = (project in file("modules/logic"))
    .settings(scalaVersion := Versions.scalaVersion)
    .settings(libraryDependencies ++= Dependencies.logicDependencies)

lazy val root = (project in file("."))
    .enablePlugins(PlayScala)
    .dependsOn(logic)
    .aggregate(logic)
