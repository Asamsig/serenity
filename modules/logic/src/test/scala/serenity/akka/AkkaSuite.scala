package serenity.akka

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, FunSpecLike}

abstract class AkkaSuite(systemName: String) extends TestKit(ActorSystem(systemName))
    with FunSpecLike with ImplicitSender with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

}
