name := "serenity"

version in ThisBuild := "0.0.1-SNAPSHOT"

scalaVersion in ThisBuild  := Versions.scalaVersion

resolvers in ThisBuild += "Atlassian Releases" at "https://maven.atlassian.com/public/"
resolvers in ThisBuild += Resolver.jcenterRepo

lazy val logic = (project in file("modules/logic"))
    .settings(scalaVersion := Versions.scalaVersion)
    .settings(libraryDependencies ++= Dependencies.logicDependencies)

lazy val root = (project in file("."))
    .enablePlugins(PlayScala)
    .settings(
      libraryDependencies ++= Dependencies.playDependencies
    )
    .dependsOn(logic)
    .aggregate(logic)
