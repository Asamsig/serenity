package models

import java.util.UUID

case class UserId(underling: UUID) {

  def asString = underling.toString

}

object UserId {

  def parseUnsafe(s: String) =
    UserId(UUID.fromString(s))

}