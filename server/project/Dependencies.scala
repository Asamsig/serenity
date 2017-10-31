import com.trueaccord.scalapb.compiler.{Version => SbVersion}
import play.sbt.PlayImport
import sbt.{Resolver, _}

object Versions {
  val akka            = "2.4.17"
  val silhouette      = "4.0.0"
  val scalaVersion    = "2.11.8"
  val protoBuf        = "3.1.0"
  val playSlick       = "2.1.0"
  val postgresql      = "9.4.1212"
  val sangria         = "1.2.0"
  val sangriaPlayJson = "1.0.0"
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
    "com.typesafe.akka"         %% "akka-persistence"                    % Versions.akka,
    "com.typesafe.akka"         %% "akka-persistence-query-experimental" % Versions.akka,
    "com.typesafe.akka"         %% "akka-remote"                         % Versions.akka,
    "org.iq80.leveldb"          % "leveldb"                              % "0.7",
    "com.github.dnvriend"       %% "akka-persistence-jdbc"               % "2.4.17.1",
    "org.postgresql"            % "postgresql"                           % Versions.postgresql,
    "org.fusesource.leveldbjni" % "leveldbjni-all"                       % "1.8",
    "com.google.protobuf"       % "protobuf-java"                        % Versions.protoBuf
  )

  private val sangria = Seq(
    "org.sangria-graphql" %% "sangria"           % Versions.sangria,
    "org.sangria-graphql" %% "sangria-play-json" % Versions.sangriaPlayJson
  )

  private val test = Seq(
    "org.scalatestplus.play" %% "scalatestplus-play"          % "2.0.0"       % Test,
    "com.typesafe.akka"      %% "akka-testkit"                % Versions.akka % Test,
    "com.github.dnvriend"    %% "akka-persistence-inmemory"   % "1.3.18"      % Test,
    "org.scalamock"          %% "scalamock-scalatest-support" % "3.5.0"       % Test
  )

  private val silhouette = Seq(
    "com.mohiva" %% "play-silhouette"                 % Versions.silhouette,
    "com.mohiva" %% "play-silhouette-password-bcrypt" % Versions.silhouette,
    "com.mohiva" %% "play-silhouette-persistence"     % Versions.silhouette,
    "com.mohiva" %% "play-silhouette-crypto-jca"      % Versions.silhouette,
    "com.mohiva" %% "play-silhouette-testkit"         % Versions.silhouette % "test"
  )

  private val root = Seq(
    "com.iheart"        %% "ficus"                 % "1.2.6",
    "net.codingwell"    %% "scala-guice"           % "4.0.1",
    "org.postgresql"    % "postgresql"             % Versions.postgresql,
    "com.typesafe.play" %% "play-slick"            % Versions.playSlick,
    "com.typesafe.play" %% "play-slick-evolutions" % Versions.playSlick,
    PlayImport.evolutions
  )

  val playDependencies = akka ++ persistence ++ test ++ silhouette ++ sangria ++ root

  val protobufDependencies = Seq(
    "com.trueaccord.scalapb" %% "scalapb-runtime" % SbVersion.scalapbVersion % "protobuf"
  )
}
