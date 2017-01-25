name := "serenity"

version in ThisBuild := "0.0.1-SNAPSHOT"

scalaVersion in ThisBuild  := Versions.scalaVersion

resolvers in ThisBuild += "Atlassian Releases" at "https://maven.atlassian.com/public/"
resolvers in ThisBuild += Resolver.jcenterRepo

lazy val logic = (project in file("modules/logic"))
    .settings(libraryDependencies ++= Dependencies.logicDependencies)
    .settings(PB.targets in Compile := Seq(
      PB.gens.java -> (sourceManaged in Compile).value,
      scalapb.gen(javaConversions=true) -> (sourceManaged in Compile).value
    ))

lazy val root = (project in file("."))
    .enablePlugins(PlayScala, ElasticBeanstalkPlugin)
    .settings(
      libraryDependencies ++= Dependencies.playDependencies
    )
    .settings(dockerExposedPorts := Seq(9000))
    .dependsOn(logic)
    .aggregate(logic)
