package repositories.eventsource

import akka.actor.{Actor, ActorRef, Props, Stash}
import akka.persistence.PersistentActor
import akka.persistence.query.journal.leveldb.scaladsl.LeveldbReadJournal
import akka.persistence.query.scaladsl.{CurrentEventsByPersistenceIdQuery, EventsByPersistenceIdQuery}
import akka.persistence.query.{EventEnvelope, PersistenceQuery}
import akka.testkit.TestProbe
import helpers.akka.{AkkaConfig, AkkaSuite, InMemoryCleanup}

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

class PersistenceIdQueryStreamSpec extends AkkaSuite("PersistenceIdsQuerySpec", AkkaConfig.inMemoryPersistence())
    with InMemoryCleanup{

  ignore("Events") { // todo: setup fault. No configured serialization-bindings for class [java.lang.Integer]
    it("should find all stored events") {
      val store = system.actorOf(Props(classOf[SomeStore]))

      store ! 1
      store ! 2

      expectMsgAllOf(Saved, Saved)
      val query = system.actorOf(Props(classOf[EventActor], None))
      query ! GetSum

      expectMsg(Sum(3))
    }

    it("should find all stored and live events") {
      val probe: TestProbe = TestProbe()
      val store = system.actorOf(Props(classOf[SomeStore]))

      store ! 1
      store ! 2
      expectMsgAllOf(Saved, Saved)

      val query = system.actorOf(Props(classOf[EventActor], Some(probe.ref)))
      probe.expectMsgAllOf(1, 2)

      store ! 3
      expectMsg(Saved)
      probe.expectMsg(10 seconds, 3)

      query ! GetSum
      expectMsg(Sum(6))
    }

  }

}

class SomeStore extends PersistentActor {
  override val persistenceId: String = "PersistenceIdsQuerySpec"

  override def receiveRecover: Receive = {
    case _ =>
  }

  override def receiveCommand: Receive = {
    case m: Int => persist(m) { evt => sender() ! Saved}
  }

}

class EventActor(to: Option[ActorRef] = None) extends PersistenceIdQueryStream with Actor with Stash {
  import repositories.eventsource.QueryStream.LiveEvents

  override val persistenceId: String = "PersistenceIdsQuerySpec"

  override def journal: CurrentEventsByPersistenceIdQuery with EventsByPersistenceIdQuery =
    PersistenceQuery(context.system).readJournalFor[LeveldbReadJournal](LeveldbReadJournal.Identifier)

  var sum = 0

  override def receive: Receive = {
    case LiveEvents => {
      unstashAll()
    }
    case EventEnvelope(_, _, _, v) if v.isInstanceOf[Int] => {
      val t = sum
      sum = sum + v.asInstanceOf[Int]
      to.foreach(_ ! v)
    }
    case GetSum => {
      if (!live) stash()
      else sender() ! Sum(sum)
    }
    case IsLive => if (live) sender() ! live else stash()
  }
}

case object Saved
case object GetSum
case object IsLive

case class Sum(v: Int)
