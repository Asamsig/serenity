package cqrs

import java.util.UUID

import akka.actor.{Actor, ActorRef, Props, Stash}
import akka.persistence.PersistentActor
import akka.persistence.query.{EventEnvelope, PersistenceQuery}
import akka.persistence.query.journal.leveldb.scaladsl.LeveldbReadJournal
import akka.persistence.query.scaladsl.{CurrentEventsByPersistenceIdQuery, EventsByPersistenceIdQuery}
import akka.testkit.TestProbe
import serenity.akka.AkkaSuite

class PersistenceIdQueryStreamSpec extends AkkaSuite("PersistenceIdsQuerySpec") {

  describe("Events") {
    it("should fin all stored events") {
      val id: String = createId()
      val store = system.actorOf(Props(classOf[SomeStore], id))

      store ! 1
      store ! 2

      expectMsgAllOf(Saved, Saved)
      val query = system.actorOf(Props(classOf[EventActor], id, None))
      query ! GetSum

      expectMsg(3)
    }

    it("should find all stored and live events") {
      val id: String = createId()
      val probe: TestProbe = TestProbe()
      val store = system.actorOf(Props(classOf[SomeStore], id))

      store ! 1
      store ! 2
      expectMsgAllOf(Saved, Saved)

      val query = system.actorOf(Props(classOf[EventActor], id, Some(probe.ref)))
      probe.expectMsgAllOf(1, 2)

      store ! 3
      expectMsg(Saved)
      probe.expectMsg(3)

      query ! GetSum
      expectMsg(6)
    }

  }

  def createId(id: UUID = UUID.randomUUID()) = s"PersistenceIdsQuerySpec-${id.toString}"
}

class SomeStore(id: String) extends PersistentActor {
  override val persistenceId: String = id

  override def receiveRecover: Receive = {
    case _ =>
  }

  override def receiveCommand: Receive = {
    case m: Int => persist(m) { evt => println(s"saved $m"); sender() ! Saved}
    case m => println(s"Unhandled in store $m")
  }

}

class EventActor(id: String, to: Option[ActorRef] = None) extends PersistenceIdQueryStream with Actor with Stash {
  import QueryStream.LiveEvents

  override val persistenceId: String = id

  override def journal: CurrentEventsByPersistenceIdQuery with EventsByPersistenceIdQuery =
    PersistenceQuery(context.system).readJournalFor[LeveldbReadJournal](LeveldbReadJournal.Identifier)

  var sum = 0

  override def receive: Receive = {
    case LiveEvents => {
      println(s"going live with $sum")
      unstashAll()
    }
    case EventEnvelope(_, _, _, v) if v.isInstanceOf[Int] => {
      val t = sum
      sum = sum + v.asInstanceOf[Int]
      println(s"$t -> $sum after $v")
      to.foreach(_ ! v)
    }
    case GetSum => {
      if (!live) stash()
      else sender() ! sum
    }
    case IsLive => if (live) sender() ! live else stash()
    case m => println(s"unhandled in event actor: $m")
  }
}

case object Saved
case object GetSum
case object IsLive