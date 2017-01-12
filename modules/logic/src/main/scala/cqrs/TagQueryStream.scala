package cqrs

import akka.NotUsed
import akka.persistence.query.EventEnvelope
import akka.persistence.query.scaladsl.{CurrentEventsByTagQuery, EventsByTagQuery}
import akka.stream.scaladsl.Source

trait TagQueryStream extends QueryStream {

  def tagName: String

  def journal: CurrentEventsByTagQuery with EventsByTagQuery

  override def streamCurrent: Source[EventEnvelope, NotUsed] =
    journal.currentEventsByTag(tagName, currentSequenceNumber)

  override def streamLive: Source[EventEnvelope, NotUsed] =
    journal.eventsByTag(tagName, currentSequenceNumber + 1)

}

