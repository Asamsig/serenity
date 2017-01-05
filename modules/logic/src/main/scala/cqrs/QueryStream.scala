package cqrs

import akka.NotUsed
import akka.actor.Actor
import akka.persistence.query.EventEnvelope
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}

trait QueryStream extends Actor {

  import QueryStream._

  def streamCurrent: Source[EventEnvelope, NotUsed]

  def streamLive: Source[EventEnvelope, NotUsed]

  private var _curSeqNum: Long = 0
  private var _live = false

  def live = _live
  def currentSequenceNumber = _curSeqNum

  override def preStart() = {
    super.preStart()
    streamCurrent.runWith(Sink.actorRef(self, LiveEvents))(ActorMaterializer()(context.system))
  }

  override def aroundReceive(receive: Receive, msg: Any): Unit = {
    super.aroundReceive(receive, msg)
    handleStreamEvents(msg)
  }

  def handleStreamEvents(msg: Any): Unit = msg match {
    case evt@EventEnvelope(_, _, snr, _) =>
      _curSeqNum = snr
    case LiveEvents =>
      println("Going live!")
      streamLive.runWith(Sink.actorRef(self, LiveEvents))(ActorMaterializer()(context.system))
      _live = true
    case _ =>

  }
}

object QueryStream {

  case object LiveEvents

}
