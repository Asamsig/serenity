package serenity.users

import java.time.LocalDateTime

import serenity.UtcDateTime
import serenity.cqrs.{Cmd, Evt, Query}
import serenity.users.domain.{BasicAuth, Email, User, UserId}

object UserProtocol {

  object write {

    case class CreateUserCmd(
        email: String,
        firstName: String,
        lastName: String
    ) extends Cmd

    case class UserRegisteredEvt(
        id: UserId,
        email: String,
        firstName: String,
        lastName: String,
        createdTime: LocalDateTime = UtcDateTime.nowUTC()
    ) extends Evt

    case class ValidationFailed(msg: String) extends Exception(msg)

    case class HospesUser(
        originId: List[Int],
        email: List[Email],
        firstname: Option[String],
        lastname: Option[String],
        address: Option[String],
        phonenumber: Option[String],
        password_pw: String,
        password_slt: String,
        memberships: Set[HospesMembership])

    case class HospesMembership(
        id: Int,
        year: Int)

    case class HospesImportCmd(user: HospesUser) extends Cmd

    case class BasicAuthEvt(
        id: UserId,
        password: String,
        salt: String,
        source: AuthSource = SerenityAuthSource) extends Evt

    sealed trait AuthSource

    case object SerenityAuthSource extends AuthSource

    case object HospesAuthSource extends AuthSource

    case class HospesUserImportEvt(
        id: UserId,
        originId: List[Int],
        email: List[Email],
        firstName: Option[String],
        lastName: Option[String],
        address: Option[String],
        phoneNumber: Option[String],
        imported: LocalDateTime = UtcDateTime.nowUTC()
    ) extends Evt

    def toHospesUserEvent(id: UserId, hospesUser: HospesUser): HospesUserImportEvt = {
      HospesUserImportEvt(
        id,
        hospesUser.originId,
        hospesUser.email,
        hospesUser.firstname,
        hospesUser.lastname,
        hospesUser.address,
        hospesUser.phonenumber
      )
    }
  }

  object read {

    case class GetUser(id: UserId) extends Query

    case class GetUserWithEmail(value: String) extends Query

    case class GetUserCredentials(email: String) extends Query

    case class UserCredentialsResponse(user: BasicAuth) extends Query

    case class UserResponse(user: User)

    case object UserNotFound

    case object CredentialsNotFound

  }

}

