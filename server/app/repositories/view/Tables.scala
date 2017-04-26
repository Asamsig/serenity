package repositories.view

import java.time.{LocalDate, LocalDateTime}

import models.user.Memberships.MembershipIssuer
import models.user.Roles.Role
import models.user.UserId
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

trait Tables extends HasDatabaseConfigProvider[JdbcProfile] with ColumnTypeMappers {

  import profile.api._

  val usersTable           = TableQuery[UsersTable]
  val userEmailsTable      = TableQuery[UserEmailsTable]
  val userCredentialsTable = TableQuery[UserCredentialsTable]
  val userMembershipsTable = TableQuery[UserMembershipsTable]
  val userRoleTable        = TableQuery[UserRolesTable]

  type UserRow = (
      UserId,
      Option[String],
      Option[String],
      Option[String],
      Option[String],
      LocalDateTime,
      LocalDateTime
  )

  class UsersTable(val tag: Tag) extends Table[UserRow](tag, "users") {
    val userId    = column[UserId]("user_id", O.PrimaryKey)
    val firstName = column[Option[String]]("first_name")
    val lastName  = column[Option[String]]("last_name")
    val phone     = column[Option[String]]("phone")
    val address   = column[Option[String]]("address")
    val created   = column[LocalDateTime]("created")
    val updated   = column[LocalDateTime]("updated")

    override def * = (
      userId,
      firstName,
      lastName,
      phone,
      address,
      created,
      updated
    )
  }

  type UserEmailRow = (UserId, String, Boolean, Boolean)

  class UserEmailsTable(val tag: Tag) extends Table[UserEmailRow](tag, "user_emails") {

    val userId       = column[UserId]("user_id", O.PrimaryKey)
    val email        = column[String]("email")
    val primaryEmail = column[Boolean]("primary_address")
    val validated    = column[Boolean]("validated")

    override def * = (
      userId,
      email,
      primaryEmail,
      validated
    )
  }

  type UserCredentialsRow = (UserId, Int, String, Option[String])

  class UserCredentialsTable(val tag: Tag)
      extends Table[UserCredentialsRow](tag, "user_credentials") {

    val userId   = column[UserId]("user_id", O.PrimaryKey)
    val typ      = column[Int]("type")
    val password = column[String]("password")
    val salt     = column[Option[String]]("salt")

    override def * = (
      userId,
      typ,
      password,
      salt
    )
  }

  type UserMembershipRow = (
      UserId,
      LocalDate,
      LocalDate,
      MembershipIssuer.Issuer,
      Option[String],
      Option[String],
      Option[String]
  )

  class UserMembershipsTable(val tag: Tag)
      extends Table[UserMembershipRow](tag, "user_memberships") {

    val userId       = column[UserId]("user_id")
    val validFrom    = column[LocalDate]("valid_from")
    val validTo      = column[LocalDate]("valid_to")
    val issuer       = column[MembershipIssuer.Issuer]("issuer")
    val ebAttendeeId = column[Option[String]]("eb_attendee_id")
    val ebEventId    = column[Option[String]]("eb_event_id")
    val ebOrderId    = column[Option[String]]("eb_order_id")

    override def * = (
      userId,
      validFrom,
      validTo,
      issuer,
      ebAttendeeId,
      ebEventId,
      ebOrderId
    )
  }

  type UserRoleRow = (UserId, Role)

  class UserRolesTable(val tag: Tag) extends Table[UserRoleRow](tag, "user_roles") {
    val userId = column[UserId]("user_id")
    val role   = column[Role]("role")

    override def * = (userId, role)
  }

}
