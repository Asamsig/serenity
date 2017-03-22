package cqrs

import akka.NotUsed
import akka.persistence.query.scaladsl.{CurrentEventsByTagQuery2, EventsByTagQuery2}
import akka.persistence.query.{EventEnvelope2, Sequence}
import akka.stream.scaladsl.Source

trait TagQueryStream extends QueryStream[EventEnvelope2] {

  def tagName: String

  def journal: CurrentEventsByTagQuery2 with EventsByTagQuery2

  override def streamCurrent: Source[EventEnvelope2, NotUsed] =
    journal.currentEventsByTag(tagName, Sequence(currentSequenceNumber))

  override def streamLive: Source[EventEnvelope2, NotUsed] =
    journal.eventsByTag(tagName, Sequence(currentSequenceNumber + 1))

}

