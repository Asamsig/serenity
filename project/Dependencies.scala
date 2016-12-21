import sbt._

object Versions {
  val akka = "2.4.11"
}

object Dependencies {
  val akka = Seq(
    "com.typesafe.akka" %% "akka-actor" % Versions.akka,
    "com.typesafe.akka" %% "akka-persistence" % Versions.akka,
    "com.typesafe.akka" %% "akka-persistence-query-experimental" % Versions.akka,
    "org.iq80.leveldb" % "leveldb" % "0.7",
    "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8"
  )

  val test = Seq(
    "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % "test"
  )

  val dependencies = akka ++ test
}