package serenity.cqrs

import java.time.LocalDateTime

import serenity.UtcDateTime

trait Evt {
  def meta(): EventMeta
}
trait Cmd
trait Query

case class EventMeta(created: LocalDateTime = UtcDateTime.nowUTC())