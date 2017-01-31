package serenity.users

import java.time.{LocalDate, LocalDateTime}
import java.util.UUID

import com.mohiva.play.silhouette.api.Identity

object domain {

  type UserId = UUID
  type DateTime = LocalDateTime
  type Date = LocalDate

  sealed trait BasicAuth {
    def password: String
    def salt: String
  }

  case class HospesAuth(
      password: String,
      salt: String) extends BasicAuth

  case class SerenityAuth(
      password: String,
      salt: String) extends BasicAuth

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

      roles: Set[Role] = Set(),
      memberships: Set[Membership] = Set()) extends Identity

  case class Email(
      address: String,
      validated: Boolean)

  trait Role {
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
