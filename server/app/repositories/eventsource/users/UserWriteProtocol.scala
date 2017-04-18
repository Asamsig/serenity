package repositories.eventsource.users

import java.time.LocalDate

import models.{Attendee, Cmd, EventMeta, Evt}
import repositories.eventsource.users.domain.{Email, EventbriteMeta, MembershipIssuer, UserId}

object UserWriteProtocol {

  case class CreateOrUpdateUserCmd(
      attendee: Attendee
  ) extends Cmd

  case class UserUpdatedEvt(
      id: UserId,
      email: String,
      firstName: String,
      lastName: String,
      phone: String,
      meta: EventMeta
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
      memberships: Set[HospesMembership]
  )

  case class HospesMembership(
      id: Int,
      year: Int
  )

  case class HospesImportCmd(user: HospesUser) extends Cmd

  case class BasicAuthEvt(
      id: UserId,
      password: String,
      salt: Option[String] = None,
      source: AuthSource = SerenityAuthSource,
      meta: EventMeta = EventMeta()
  ) extends Evt

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
      meta: EventMeta = EventMeta()
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

  object MembershipAction extends Enumeration {
    type Action = Value
    val Add = Value
    val Remove = Value
  }

  case class MembershipUpdateEvt(
      from: LocalDate,
      action: MembershipAction.Action,
      issuer: MembershipIssuer.Issuer,
      eventbirteMeta: Option[EventbriteMeta],
      meta: EventMeta = EventMeta()
  ) extends Evt

  case class UpdateCredentialsCmd(email: String, hashedPassword: String) extends Cmd

}

