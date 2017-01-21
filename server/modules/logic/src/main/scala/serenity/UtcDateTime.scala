package serenity

import java.time.{Clock, LocalDateTime}

object UtcDateTime {

  def nowUTC(): LocalDateTime = LocalDateTime.now(Clock.systemUTC())

}
