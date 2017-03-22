name := "serenity"

version in ThisBuild := "0.0.1-SNAPSHOT"

lazy val logic = (project in file("modules/logic"))
    .settings(CommonSettings.projectSettings: _*)
    .settings(libraryDependencies ++= Dependencies.logicDependencies)
    .settings(PB.targets in Compile := Seq(
      PB.gens.java -> (sourceManaged in Compile).value,
      scalapb.gen(javaConversions=true) -> (sourceManaged in Compile).value
    ))

lazy val root = (project in file("."))
    .enablePlugins(PlayScala, ElasticBeanstalkPlugin)
    .settings(CommonSettings.projectSettings: _*)
    .settings(libraryDependencies ++= Dependencies.playDependencies)
    .settings(dockerExposedPorts := Seq(9000))
    .dependsOn(logic)
    .aggregate(logic)
