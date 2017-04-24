package models.user

import java.time.LocalDateTime

import com.mohiva.play.silhouette.api.Identity
import models.user.Memberships.Membership
import models.user.Roles.Role

case class User(
    userId: UserId,
    mainEmail: Email,
    emails: Seq[Email] = Seq(),
    phone: Option[String] = None,

    createdDate: LocalDateTime,
    firstName: Option[String] = None,
    lastName: Option[String] = None,
    address: Option[String] = None,

    roles: Set[Role] = Set(),
    memberships: Set[Membership] = Set()
) extends Identity {

  def allEmail = mainEmail :: emails.toList
}
