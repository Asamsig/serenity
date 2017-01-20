package serenity.users

import akka.actor.Props
import akka.actor.Status.{Failure, Success}
import akka.persistence.PersistentActor
import serenity.UtcDateTime.nowUTC
import serenity.cqrs.Evt
import serenity.users.UserProtocol.read._
import serenity.users.UserProtocol.write.{HospesAuthSource, _}
import serenity.users.domain._

class UserActor(id: UserId) extends PersistentActor {

  private var user: Option[User] = None
  private var credentials: Option[BasicAuth] = None

  override def persistenceId: String = s"user-${id.toString}"

  override def receiveRecover: Receive = {
    case msg: UserRegisteredEvt => updateUserModel(msg)
    case msg: HospesUserImportEvt => updateUserModel(msg)
  }

  override def receiveCommand: Receive = {
    case cmd: HospesImportCmd if user.isDefined =>
      sender() ! Failure(ValidationFailed("User exist"))
    case HospesImportCmd(hospesUser) =>
      persistAll(List(
        toHospesUserEvent(id, hospesUser),
        BasicAuthEvt(id, hospesUser.password_pw, hospesUser.password_slt, HospesAuthSource)
      )) {
        case evt: HospesUserImportEvt =>
          updateUserModel(evt)
          sender() ! Success("User created")
        case evt: BasicAuthEvt =>
          credentials = Some(HospesAuth(evt.password, evt.salt))
      }
    case cmd@CreateUserCmd(email, _, _) if user.isDefined =>
      sender() ! Failure(ValidationFailed("User exist"))
    case cmd@CreateUserCmd(email, firstName, lastName) =>
      persist(
        UserRegisteredEvt(id, email, firstName, lastName, nowUTC())) {
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

  private def updateUserModel(evt: Evt) = evt match {
    case evt: HospesUserImportEvt => user = EventToUser(evt)
    case evt: UserRegisteredEvt => user = EventToUser(evt)
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
    evt.imported,
    evt.firstName,
    evt.lastName,
    evt.address))

  def apply(evt: UserRegisteredEvt): Option[User] = {
    Some(
      User(
        uuid = evt.id,
        mainEmail = Email(evt.email, validated = true),
        createdDate = evt.createdTime,
        firstName = Some(evt.firstName)
      ))

  }
}