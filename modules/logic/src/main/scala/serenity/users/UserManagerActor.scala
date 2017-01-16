package serenity.users

import java.util.UUID

import akka.actor.{ActorRef, Props, Stash}
import akka.persistence.query.scaladsl.{CurrentEventsByTagQuery, EventsByTagQuery}
import akka.persistence.query.{EventEnvelope, PersistenceQuery}
import cqrs.QueryStream.LiveEvents
import serenity.cqrs.Cmd
import cqrs.TagQueryStream
import serenity.persistence.Tags
import serenity.users.UserProtocol.read.{GetUser, GetUserWithEmail}
import serenity.users.UserProtocol.write._
import serenity.users.domain._

import scala.util.Failure

class UserManagerActor(userActorProps: UserId => Props) extends TagQueryStream with Stash {

  override val tagName: String = Tags.USER_EMAIL

  override def journal: CurrentEventsByTagQuery with EventsByTagQuery = {
    val journalPluginId = context.system.settings.config.getString("serenity.persistence.query-journal")
    PersistenceQuery(context.system).readJournalFor(journalPluginId)
  }

  var state = UserManagerState()

  override def receive: Receive = events orElse commands orElse query

  def commands: Receive = {
    case cmd: CreateUserCmd if state.emailExists(cmd.email) =>
      sender() ! Failure(ValidationFailed("User exist"))
    case cmd: CreateUserCmd =>
      createAccount(cmd, cmd.email)

    case cmd@HospesImportCmd(usr) if state.emailExists(usr.email.map(_.address)) =>
      sender() ! Failure(ValidationFailed("User exist"))
    case cmd@HospesImportCmd(usr) =>
      createAccount(cmd, usr.email.head.address)
  }

  def events: Receive = {
    case EventEnvelope(_, _, _, e) => e match {
      case u: HospesUserImportEvt =>
        state = state.mailEvent(u)
        if (live) unstashAll()
      case u: UserRegisteredEvt =>
        state = state.mailEvent(u)
        if (live) unstashAll()
    }
    case LiveEvents =>
      unstashAll()
    case m if !live => stash()
  }

  def query: Receive = {
    case GetUserWithEmail(email) if state.pendingUsers.keySet.contains(email) =>
      stash()
    case GetUserWithEmail(email) =>
      (for {
        (_, id) <- state.emailToUsers.find(_._1 == email)
        userActor <- state.usersActor.find(_._1 == id).map(_._2)
      } yield (userActor, id)) match {
        case Some((actor, id)) =>
          actor.forward(GetUser(id))
        case None =>
          sender() ! Failure(ValidationFailed("User with email doesn't exist"))
      }
  }

  def createAccount[C <: Cmd](cmd: C, email: String): Unit = {
    val userId: UserId = UUID.randomUUID()
    val userActor: ActorRef = context.actorOf(userActorProps(userId))
    state = state.createActor(userId, email, userActor)
    userActor.forward(cmd)
  }

}

object UserManagerActor {
  def apply(userActorProps: UserId => Props = UserActor.apply): Props =
    Props(classOf[UserManagerActor], userActorProps)
}

case class UserManagerState(
    pendingUsers: Map[String, UserId] = Map(),
    emailToUsers: Map[String, UserId] = Map(),
    usersActor: Map[UserId, ActorRef] = Map()) {

  def createActor(id: UserId, email: String, actorRef: ActorRef): UserManagerState =
    copy(
      usersActor = usersActor + (id -> actorRef),
      pendingUsers = pendingUsers + (email -> id))

  def mailEvent(evt: HospesUserImportEvt): UserManagerState =
    copy(
      emailToUsers = emailToUsers ++ evt.email.map(_.address -> evt.id),
      pendingUsers = pendingUsers.filter(p => !evt.email.map(_.address).contains(p._1)))

  def mailEvent(evt: UserRegisteredEvt): UserManagerState =
    copy(
      emailToUsers = emailToUsers + (evt.email -> evt.id),
      pendingUsers = pendingUsers - evt.email)

  def emailExists(emails: Seq[String]): Boolean =
    emailToUsers.keys.exists(e => emails.contains(e)) ||
        pendingUsers.keys.exists(e => emails.contains(e))

  def emailExists(email: String): Boolean =
    emailToUsers.keys.exists(e => email == e) ||
        pendingUsers.keys.exists(e => email == e)

}