package models

import java.time.LocalDateTime

trait Evt {
  def meta(): EventMeta
}
trait Cmd
trait Query

case class EventMeta(created: LocalDateTime = time.dateTimeNow())