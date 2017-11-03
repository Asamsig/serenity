package repositories.eventsource

import akka.NotUsed
import akka.persistence.query.scaladsl.{CurrentEventsByTagQuery, EventsByTagQuery}
import akka.persistence.query.{EventEnvelope, Sequence}
import akka.stream.scaladsl.Source

trait TagQueryStream extends QueryStream[EventEnvelope] {

  def tagName: String

  def journal: CurrentEventsByTagQuery with EventsByTagQuery

  override def streamCurrent: Source[EventEnvelope, NotUsed] =
    journal.currentEventsByTag(tagName, Sequence(currentSequenceNumber))

  override def streamLive: Source[EventEnvelope, NotUsed] =
    journal.eventsByTag(tagName, Sequence(currentSequenceNumber + 1))

}
