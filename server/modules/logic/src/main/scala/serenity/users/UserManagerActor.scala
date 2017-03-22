package serenity.users

import java.util.UUID

import akka.actor.{ActorLogging, ActorRef, Props, Stash}
import akka.persistence.query.scaladsl.{CurrentEventsByTagQuery2, EventsByTagQuery2}
import akka.persistence.query.{EventEnvelope2, PersistenceQuery}
import cqrs.QueryStream.LiveEvents
import cqrs.TagQueryStream
import serenity.cqrs.Cmd
import serenity.persistence.{DomainReadEventAdapter, Tags}
import serenity.users.UserReadProtocol.{CredentialsNotFound, GetUser, GetUserCredentials, GetUserWithEmail}
import serenity.users.UserWriteProtocol._
import serenity.users.domain._

import scala.util.Failure

class UserManagerActor(userActorProps: UserId => Props) extends TagQueryStream with Stash with ActorLogging {

  override val tagName: String = Tags.USER_EMAIL

  val toMsg = new DomainReadEventAdapter

  override def journal: CurrentEventsByTagQuery2 with EventsByTagQuery2 = {
    val journalPluginId = context.system.settings.config.getString("serenity.persistence.query-journal")
    PersistenceQuery(context.system).readJournalFor(journalPluginId)
  }

  var state = UserManagerState()

  override def receive: Receive = events orElse commands orElse query

  def commands: Receive = {
    case cmd: CreateOrUpdateUserCmd if state.emailExists(cmd.attendee.profile.email) =>
      forwardToActor(cmd, cmd.attendee.profile.email)
    case cmd: CreateOrUpdateUserCmd =>
      createAccount(cmd, cmd.attendee.profile.email)

    case cmd@HospesImportCmd(usr) if state.emailExists(usr.email.map(_.address)) =>
      sender() ! Failure(ValidationFailed("User exist"))
    case cmd@HospesImportCmd(usr) =>
      createAccount(cmd, usr.email.head.address)
  }

  def events: Receive = {
    case ee@EventEnvelope2(_, _, _, e) => toMsg.fromMessage(e) match {
      case u: HospesUserImportEvt =>
        state = state.mailEvent(u)
        if (live) unstashAll()
      case u: UserUpdatedEvt =>
        state = state.mailEvent(u)
        if (live) unstashAll()
      case m =>
        unhandled(ee)
    }
    case LiveEvents =>
      log.info(s"Loaded ${state.emailToUsers.values.toSet.size} users")
      unstashAll()
    case m if !live =>
      stash()
  }

  def query: Receive = {
    case GetUserWithEmail(email) if state.pendingUsers.keySet.contains(email) =>
      stash()
    case GetUserWithEmail(email) =>
      state.emailToUsers.get(email)
          .map(id => (id, state.usersActor.getOrElse(id, userActorFromId(id)))) match {
        case Some((id, actor)) =>
          actor.forward(GetUser(id))
        case None =>
          sender() ! Failure(ValidationFailed("User with email doesn't exist"))
      }
    case qry@GetUserCredentials(email) => {
      if (state.pendingUsers.keySet.contains(email)) {
        stash()
      } else {
        state.emailToUsers.get(email)
            .map(id => (id, state.usersActor.getOrElse(id, userActorFromId(id)))) match {
          case Some((id, actor)) =>
            actor.forward(qry)
          case None =>
            sender() ! CredentialsNotFound
        }
      }
    }
  }

  def createAccount[C <: Cmd](cmd: C, email: String): Unit = {
    val userId: UserId = UUID.randomUUID()
    val userActor: ActorRef = context.actorOf(userActorProps(userId))
    state = state.createActor(userId, email, userActor)
    userActor.forward(cmd)
  }

  def forwardToActor[C <: Cmd](cmd: C, email: String): Unit = {
    state.emailToUsers.get(email)
        .map(id => state.usersActor.getOrElse(id, {
          userActorFromId(id)
        }))
        .foreach(_.forward(cmd))
  }

  private def userActorFromId[C <: Cmd](id: UserId) = {
    val actor = context.actorOf(userActorProps(id))
    state = state.copy(usersActor = state.usersActor + (id -> actor))
    actor
  }
}

object UserManagerActor {
  def apply(userActorProps: UserId => Props = UserActor.apply): Props =
    Props(classOf[UserManagerActor], userActorProps)
}

case class UserManagerState(
    pendingUsers: Map[String, UserId] = Map(),
    emailToUsers: Map[String, UserId] = Map(),
    usersActor: Map[UserId, ActorRef] = Map()
) {

  def createActor(id: UserId, email: String, actorRef: ActorRef): UserManagerState =
    copy(
      usersActor = usersActor + (id -> actorRef),
      pendingUsers = pendingUsers + (email -> id))

  def mailEvent(evt: HospesUserImportEvt): UserManagerState =
    copy(
      emailToUsers = emailToUsers ++ evt.email.map(_.address -> evt.id),
      pendingUsers = pendingUsers.filter(p => !evt.email.map(_.address).contains(p._1)))

  def mailEvent(evt: UserUpdatedEvt): UserManagerState =
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