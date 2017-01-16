package serenity.akka

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.{BeforeAndAfterAll, FunSpecLike}

abstract class AkkaSuite(
    systemName: String,
    akkaConfig: Config = AkkaConfig.default)
    extends TestKit(ActorSystem(systemName, akkaConfig))
    with FunSpecLike with ImplicitSender with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

}

object AkkaConfig {

  import collection.JavaConverters._

  val default = ConfigFactory.load()

  def inMemoryPersistence() =
    ConfigFactory.parseMap(Map(
      "akka.persistence.journal.plugin" -> "inmemory-journal",
      "akka.persistence.snapshot-store.plugin" -> "inmemory-snapshot-store",
      "serenity.persistence.query-journal" -> "inmemory-read-journal"
    ).asJava, "From tests")
        .withFallback(default)

}

