package repositories.eventsource.users

import java.time.{LocalDate, LocalDateTime}
import java.util.UUID

import com.mohiva.play.silhouette.api.Identity

object domain {

  type UserId = UUID
  type DateTime = LocalDateTime
  type Date = LocalDate

  sealed trait BasicAuth {
    def password: String
    def salt: Option[String]
  }

  case class HospesAuth(
      password: String,
      salt: Option[String]) extends BasicAuth

  case class SerenityAuth(
      password: String
  ) extends BasicAuth {
    val salt: Option[String] = None
  }

  case class Membership(
      from: Date,
      to: Date,
      issuer: MembershipIssuer.Issuer,
      eventbriteMeta: Option[EventbriteMeta]
  )

  case class EventbriteMeta(
      attendeeId: String,
      eventId: String,
      orderId: String
  )

  object MembershipIssuer extends Enumeration {
    type Issuer = Value
    val JavaBin = Value
    val JavaZone = Value

    def toInt(i: Issuer): Int = i match {
      case JavaBin => 1
      case JavaZone => 2
    }

    def unsafeFromInt(i: Int): Issuer = i match {
      case 1 => JavaBin
      case 2 => JavaZone
    }
  }

  case class User(
      uuid: UserId,
      mainEmail: Email,
      emails: Seq[Email] = Seq(),
      phone: Option[String] = None,

      createdDate: DateTime,
      firstName: Option[String] = None,
      lastName: Option[String] = None,
      address: Option[String] = None,

      roles: Set[Role] = Set(),
      memberships: Set[Membership] = Set()) extends Identity {

    def allEmail = mainEmail :: emails.toList
  }

  case class Email(
      address: String,
      validated: Boolean)

  sealed trait Role {
    def name: String
  }

  object Role {
    def apply(role: String) = role match {
      case AdminRole.name => AdminRole
      case _ => UnknownRole
    }
  }

  case object AdminRole extends Role {
    val name = "admin"
  }

  case object UnknownRole extends Role {
    val name = "-"
  }

}
