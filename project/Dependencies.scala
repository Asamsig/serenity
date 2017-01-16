import sbt._

object Versions {
  val akka = "2.4.11"
  val scalaVersion = "2.11.8"
}

object Dependencies {
  private val akka = Seq(
    "com.typesafe.akka" %% "akka-actor" % Versions.akka
  )

  private val persistence = akka ++ Seq(
    "com.typesafe.akka" %% "akka-persistence" % Versions.akka,
    "com.typesafe.akka" %% "akka-persistence-query-experimental" % Versions.akka,
    "org.iq80.leveldb" % "leveldb" % "0.7",
    "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8"
  )

  private val test = Seq(
    "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % "test",
    "com.typesafe.akka"      %% "akka-testkit" % Versions.akka % "test",
    "com.github.dnvriend"    %% "akka-persistence-inmemory" % "1.3.18" % "test"
  )

  val playDependencies = akka ++ test

  val logicDependencies = akka ++ persistence ++ test
}