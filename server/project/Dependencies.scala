import com.trueaccord.scalapb.compiler.{Version => SbVersion}
import play.sbt.PlayImport
import sbt.{Resolver, _}

object Versions {
  val scalaVersion         = "2.12.4"
  val akka                 = "2.5.8"
  val akkaPersistenceJdbc  = "3.1.0"
  val akkaPersistenceInmem = "2.5.1.1"
  val levelDb              = "0.10"
  val postgresql           = "42.1.4"
  val silhouette           = "5.0.3"
  val protoBuf             = "3.4.0"
  val playVersion          = play.core.PlayVersion.current
  val playSlick            = "3.0.3"
  val slick                = "3.2.1"
  val sangria              = "1.3.3"
  val sangriaPlayJson      = "1.0.4"
  val scalaGuice           = "4.1.1"
  val ficus                = "1.4.3"
  val leveldbjni           = "1.8"
  val scalaMock            = "3.6.0"
  val scalaTestPlusPlay    = "3.1.2"
}

object Dependencies {

  val resolvers = DefaultOptions.resolvers(snapshot = true) ++ Seq(
    "Atlassian Releases" at "https://maven.atlassian.com/public/",
    Resolver.jcenterRepo
  )

  private val akka = Seq(
    "com.typesafe.akka" %% "akka-actor" % Versions.akka
  )

  private val persistence = akka ++ Seq(
    "com.typesafe.akka"         %% "akka-persistence"       % Versions.akka,
    "com.typesafe.akka"         %% "akka-persistence-query" % Versions.akka,
    "com.typesafe.akka"         %% "akka-remote"            % Versions.akka,
    "org.iq80.leveldb"          % "leveldb"                 % Versions.levelDb,
    "com.github.dnvriend"       %% "akka-persistence-jdbc"  % Versions.akkaPersistenceJdbc,
    "org.postgresql"            % "postgresql"              % Versions.postgresql,
    "org.fusesource.leveldbjni" % "leveldbjni-all"          % Versions.leveldbjni,
    "com.google.protobuf"       % "protobuf-java"           % Versions.protoBuf
  )

  private val sangria = Seq(
    "org.sangria-graphql" %% "sangria"           % Versions.sangria,
    "org.sangria-graphql" %% "sangria-play-json" % Versions.sangriaPlayJson
  )

  private val test = Seq(
    "org.scalatestplus.play" %% "scalatestplus-play"          % Versions.scalaTestPlusPlay    % Test,
    "com.typesafe.akka"      %% "akka-testkit"                % Versions.akka                 % Test,
    "com.github.dnvriend"    %% "akka-persistence-inmemory"   % Versions.akkaPersistenceInmem % Test,
    "org.scalamock"          %% "scalamock-scalatest-support" % Versions.scalaMock            % Test
  )

  private val silhouette = Seq(
    "com.mohiva" %% "play-silhouette"                 % Versions.silhouette,
    "com.mohiva" %% "play-silhouette-password-bcrypt" % Versions.silhouette,
    "com.mohiva" %% "play-silhouette-persistence"     % Versions.silhouette,
    "com.mohiva" %% "play-silhouette-crypto-jca"      % Versions.silhouette,
    "com.mohiva" %% "play-silhouette-testkit"         % Versions.silhouette % Test
  )

  private val root = Seq(
    "com.iheart"         %% "ficus"                 % Versions.ficus,
    "net.codingwell"     %% "scala-guice"           % Versions.scalaGuice,
    "org.postgresql"     % "postgresql"             % Versions.postgresql,
    "com.typesafe.play"  %% "play-slick"            % Versions.playSlick,
    "com.typesafe.play"  %% "play-slick-evolutions" % Versions.playSlick,
    "com.typesafe.slick" %% "slick"                 % Versions.slick,
    PlayImport.evolutions
  )

  private val playDeps = Seq(
    "com.typesafe.play" %% "play-guice" % Versions.playVersion
  )

  val playDependencies = playDeps ++ akka ++ persistence ++ test ++ silhouette ++ sangria ++ root

  val protobufDependencies = Seq(
    "com.trueaccord.scalapb" %% "scalapb-runtime" % SbVersion.scalapbVersion % "protobuf"
  )
}
