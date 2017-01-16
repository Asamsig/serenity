package serenity.users

import java.time.LocalDateTime
import java.util.UUID

object domain {

  type UserId = UUID
  type DateTime = LocalDateTime
  type Date = String

  case class BasicAuth(
      userId: UserId,
      password: String,
      salt: String)

  case class Membership(
      from: Date,
      to: Date,
      kind: String)

  sealed trait MembershipType
  case object Hospes
  case object JavaZone
  case object JabaBin
  case object JavaBinHero

  case class User(
      uuid: UserId,
      mainEmail: Email,
      emails: Seq[Email] = Seq(),
      phone: Option[String] = None,

      createdDate: DateTime,
      firstName: Option[String] = None,
      lastName: Option[String] = None,
      address: Option[String] = None,

      memberships: Set[Membership] = Set())

  case class Email(
      address: String,
      validated: Boolean)

}
