package cqrs

import akka.NotUsed
import akka.actor.Actor
import akka.persistence.query.{EventEnvelope, EventEnvelope2}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}

/**
 * @tparam Env EventEnvelope or EventEnvelope2. Workaround while the api isn't stable
 */
trait QueryStream[Env] extends Actor {

  import QueryStream._

  def streamCurrent: Source[Env, NotUsed]

  def streamLive: Source[Env, NotUsed]

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
    case evt@EventEnvelope2(_, _, snr, _) =>
      _curSeqNum = snr
    case evt@EventEnvelope(_, _, snr, _) =>
      _curSeqNum = snr
    case LiveEvents =>
      streamLive.runWith(Sink.actorRef(self, LiveEvents))(ActorMaterializer()(context.system))
      _live = true
    case _ =>

  }
}

object QueryStream {

  case object LiveEvents

}
