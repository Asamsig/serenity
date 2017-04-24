package models

import java.util.UUID

import scala.util.Try

case class UserId(underling: UUID) {

  def asString = underling.toString

}

object UserId {

  def fromString(str: String): Option[UserId] =
    Try(UUID.fromString(str)).map(UserId.apply).toOption

  @throws(classOf[NoSuchElementException])
  def unsafeFromString(str: String): UserId =
    fromString(str).get

  def generate(): UserId =
    UserId(UUID.randomUUID())
}