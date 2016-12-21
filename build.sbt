name := "serenity"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.11.8"

libraryDependencies ++= Dependencies.dependencies

lazy val root = (project in file("."))
    .enablePlugins(PlayScala)