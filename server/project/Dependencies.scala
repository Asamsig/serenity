import sbt._

object Versions {
  val akka = "2.4.11"
  val silhouette = "4.0.0"
  val scalaVersion = "2.11.8"
  val protoBuf = "3.1.0"
}

object Dependencies {
  private val akka = Seq(
    "com.typesafe.akka" %% "akka-actor" % Versions.akka
  )

  private val persistence = akka ++ Seq(
    "com.typesafe.akka" %% "akka-persistence" % Versions.akka,
    "com.typesafe.akka" %% "akka-persistence-query-experimental" % Versions.akka,
    "com.typesafe.akka" %% "akka-remote" % Versions.akka,
    "org.iq80.leveldb" % "leveldb" % "0.7",
    "com.github.dnvriend" %% "akka-persistence-jdbc" % "2.6.12",
    "org.postgresql" % "postgresql" % "9.4.1212",
    "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8",
    "com.google.protobuf" % "protobuf-java" % Versions.protoBuf
  )

  private val test = Seq(
    "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % "test",
    "com.typesafe.akka"      %% "akka-testkit" % Versions.akka % "test",
    "com.github.dnvriend"    %% "akka-persistence-inmemory" % "1.3.18" % "test"
  )

  private val silhouette = Seq(
    "com.mohiva" %% "play-silhouette" % Versions.silhouette,
    "com.mohiva" %% "play-silhouette-password-bcrypt" % Versions.silhouette,
    "com.mohiva" %% "play-silhouette-persistence" % Versions.silhouette,
    "com.mohiva" %% "play-silhouette-crypto-jca" % Versions.silhouette,
    "com.mohiva" %% "play-silhouette-testkit" % Versions.silhouette % "test"
  )

  private val root = Seq(
    "com.iheart" %% "ficus" % "1.2.6",
    "net.codingwell" %% "scala-guice" % "4.0.1"
  )

  val playDependencies = akka ++ test ++ silhouette ++ root

  val logicDependencies = akka ++ persistence ++ test ++ silhouette
}