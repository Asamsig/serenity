resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"

resolvers += Resolver.url(
  "bintray-kipsigman-sbt-plugins",
  url("http://dl.bintray.com/kipsigman/sbt-plugins")
)(Resolver.ivyStylePatterns)

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.6.9")

//addSbtPlugin("kipsigman" % "sbt-elastic-beanstalk" % "0.1.4-SNAPSHOT")

// Formatting and style checking
addSbtPlugin("com.geirsson" % "sbt-scalafmt" % "1.3.0")
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")
