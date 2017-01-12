package serenity.users

import java.util.Date

import akka.actor.Props
import akka.actor.Status.{Failure, Success}
import akka.persistence.PersistentActor
import akka.persistence.journal.Tagged
import cqrs.Tags
import cqrs.cqrs.Evt
import serenity.users.UserProtocol.read.{GetUser, UserNotFound, UserResponse}
import serenity.users.UserProtocol.write._
import serenity.users.domain.{Email, User, UserId}

class UserActor(id: UserId) extends PersistentActor {

  var user: Option[User] = None

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
        toHospesUserEvent(id, hospesUser)
        // todo other events!
      )) {
        case evt: HospesUserImportEvt =>
          Tagged(updateUserModel(evt), Set(Tags.USER_EMAIL))
          sender() ! Success("")
      }

    case cmd@CreateUserCmd(email, _, _) if user.isDefined =>
      sender() ! Failure(ValidationFailed("User exist"))
    case cmd@CreateUserCmd(email, firstName, lastName) =>
      persist(
        UserRegisteredEvt(id, email, firstName, lastName, new Date())) {
        evt =>
          Tagged(updateUserModel(evt), Set(Tags.USER_EMAIL))
          sender() ! Success("")
      }
    case GetUser(queriedId) if id != queriedId => sender() ! UserNotFound
    case GetUser(queriedId) => sender() ! user.map(UserResponse).getOrElse(UserNotFound)
    case m@_ => sender() ! Failure(new IllegalArgumentException("Unhandled message"))
  }

  def updateUserModel(evt: Evt) = evt match  {
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
    "now",
    evt.firstName,
    evt.lastName,
    evt.address))

  def apply(evt: UserRegisteredEvt): Option[User]= {
    Some(
      User(
        uuid = evt.id,
        mainEmail = Email(evt.email, validated = true),
        createdDate = evt.createdTime.toString,
        firstName = Some(evt.firstName)
      ))

  }
}