name := "serenity"

version in ThisBuild := "0.0.1-SNAPSHOT"

lazy val protobuf = (project in file("protobuf"))
    .settings(CommonSettings.projectSettings: _*)
    .settings(libraryDependencies ++= Dependencies.protobufDependencies)
    .settings(PB.targets in Compile := Seq(
      PB.gens.java -> (sourceManaged in Compile).value,
      scalapb.gen(javaConversions=true) -> (sourceManaged in Compile).value
    ))

lazy val root = (project in file("."))
    .enablePlugins(PlayScala, ElasticBeanstalkPlugin)
    .settings(CommonSettings.projectSettings: _*)
    .settings(libraryDependencies ++= Dependencies.playDependencies)
    .settings(dockerExposedPorts := Seq(9000))
    .dependsOn(protobuf)
    .aggregate(protobuf)
