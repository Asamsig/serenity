package cqrs

import akka.NotUsed
import akka.persistence.query.{EventEnvelope}
import akka.persistence.query.scaladsl.{CurrentEventsByPersistenceIdQuery, EventsByPersistenceIdQuery}
import akka.stream.scaladsl.Source

trait PersistenceIdQueryStream extends QueryStream[EventEnvelope] {

  def persistenceId: String

  def journal: CurrentEventsByPersistenceIdQuery with EventsByPersistenceIdQuery

  override def streamCurrent: Source[EventEnvelope, NotUsed] =
    journal.currentEventsByPersistenceId(persistenceId, currentSequenceNumber, Long.MaxValue)

  override def streamLive: Source[EventEnvelope, NotUsed] =
    journal.eventsByPersistenceId(persistenceId, currentSequenceNumber + 1, Long.MaxValue)

}

