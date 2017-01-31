package serenity.users

import java.time.LocalDate

import akka.actor.Props
import akka.actor.Status.{Failure, Success}
import akka.persistence.PersistentActor
import serenity.cqrs.{EventMeta, Evt}
import serenity.users.UserReadProtocol._
import serenity.users.UserWriteProtocol.{HospesAuthSource, _}
import serenity.users.domain._

class UserActor(id: UserId) extends PersistentActor {

  private var user: Option[User] = None
  private var credentials: Option[BasicAuth] = None

  override def persistenceId: String = s"user-${id.toString}"

  override def receiveRecover: Receive = {
    case msg: UserUpdatedEvt => updateUserModel(msg)
    case msg: HospesUserImportEvt => updateUserModel(msg)
    case msg: MembershipUpdateEvt => updateUserModel(msg)
  }

  override def receiveCommand: Receive = {
    case cmd: HospesImportCmd if user.isDefined =>
      sender() ! Failure(ValidationFailed("User exist"))
    case HospesImportCmd(hospesUser) =>
      persistAll(List(
        toHospesUserEvent(id, hospesUser),
        BasicAuthEvt(id, hospesUser.password_pw, hospesUser.password_slt, HospesAuthSource)
      ) ++ hospesUser.memberships
          .map(m => MembershipUpdateEvt(
            LocalDate.of(m.year, 1, 1),
            MembershipAction.Add,
            MembershipIssuer.JavaBin))) {
        case evt: HospesUserImportEvt =>
          updateUserModel(evt)
          sender() ! Success("User created")
        case evt: BasicAuthEvt =>
          credentials = Some(HospesAuth(evt.password, evt.salt))
        case evt: MembershipUpdateEvt =>
          updateUserModel(evt)
      }
    case cmd@CreateOrUpdateUserCmd(attendee) =>
      val p = attendee.profile
      persist(
        UserUpdatedEvt(id, p.email, p.firstName, p.lastName, p.phone, EventMeta())) {
        evt =>
          updateUserModel(evt)
          sender() ! Success("")
      }
    case GetUser(queriedId) =>
      if (id != queriedId) sender() ! UserNotFound
      else sender() ! user.map(UserResponse).getOrElse(UserNotFound)
    case GetUserCredentials(cred) =>
      if (hasEmail(cred)) sender() ! credentials.map(UserCredentialsResponse).getOrElse(CredentialsNotFound)
      else sender() ! CredentialsNotFound
    case m@_ => sender() ! Failure(new IllegalArgumentException("Unhandled message"))
  }

  private def hasEmail(email: String): Boolean =
    user.exists(_.emails.exists(_.address == email)) ||
        user.exists(_.mainEmail.address == email)

  private def updateUserModel(evt: Evt) =
    evt match {
      case evt: HospesUserImportEvt => user = EventToUser(evt)
      case evt: UserUpdatedEvt => user = EventToUser(evt, user)
      case evt: MembershipUpdateEvt => user = user.map(u => u.copy(
        memberships =
            evt.action match {
              case MembershipAction.Add =>
                u.memberships + Membership(evt.from, evt.from.plusYears(1).minusDays(1), evt.issuer.toString)
              case MembershipAction.Remove =>
                u.memberships - Membership(evt.from, evt.from.plusYears(1).minusDays(1), evt.issuer.toString)
            }
      ))
      case _ =>
    }
}

object UserActor {

  def apply(id: UserId): Props = Props.create(classOf[UserActor], id)

}

object EventToUser {

  def apply(evt: HospesUserImportEvt): Option[User] = Some(User(
    evt.id,
    evt.email.head,
    evt.email.tail,
    evt.phoneNumber,
    evt.meta.created,
    evt.firstName,
    evt.lastName,
    evt.address))

  def apply(evt: UserUpdatedEvt, user: Option[User]): Option[User] = {
    user.map(_.copy(
      firstName = Some(evt.firstName),
      lastName = Some(evt.lastName)
    )).orElse(Some(
      User(
        uuid = evt.id,
        mainEmail = Email(evt.email, validated = true),
        createdDate = evt.meta.created,
        firstName = Some(evt.firstName),
        lastName = Some(evt.lastName)
      )))

  }
}