package serenity.users

import java.util.UUID

import akka.actor.{ActorRef, Props}
import akka.persistence.PersistentActor
import cqrs.cqrs.{Cmd, Evt}
import serenity.users.UserProtocol.read.{GetUser, GetUserWithEmail}
import serenity.users.UserProtocol.write.{CreateUserCmd, HospesImportCmd, ValidationFailed}
import serenity.users.domain._

import scala.util.Failure

class UserManagerActor(userActorProps: UserId => Props) extends PersistentActor {

  var emailToUsers: Map[String, UserId] = Map()
  var usersActor: Map[UserId, ActorRef] = Map()

  override def persistenceId: String = "users"

  override def receiveRecover: Receive = events

  override def receiveCommand: Receive = commands orElse events orElse query

  def commands: Receive = {
    case cmd: CreateUserCmd if emailToUsers.contains(cmd.email) =>
      sender() ! Failure(ValidationFailed("User exist"))
    case cmd: CreateUserCmd =>
      createAccount(cmd, cmd.email)

    case cmd@HospesImportCmd(usr) if emailToUsers.exists(usr.email.map(_.address) contains _._1) =>
      sender() ! Failure(ValidationFailed("User exist"))
    case cmd@HospesImportCmd(usr) =>
      createAccount(cmd, usr.email.head.address)
  }

  def events: Receive = {
    case m: Evt => unhandled(m)
  }

  def query: Receive = {
    case GetUserWithEmail(email) =>
      (for {
        (email, id) <- emailToUsers.find(_._1 == email)
        userActor <- usersActor.find(_._1 == id).map(_._2)
      } yield (userActor, id)) match {
        case Some((actor, id)) => actor.forward(GetUser(id))
        case None => sender() ! Failure(ValidationFailed("User with email doesn't exist"))
      }
  }

  def createAccount[C <: Cmd](cmd: C, email: String): Unit = {
    val userId: UserId = UUID.randomUUID()
    val userActor: ActorRef = context.actorOf(userActorProps(userId))
    emailToUsers = emailToUsers + (email -> userId)
    usersActor = usersActor + (userId -> userActor)
    userActor.forward(cmd)
  }

}

object UserManagerActor {
  def apply(userActorProps: UserId => Props = UserActor.apply): Props =
    Props(classOf[UserManagerActor], userActorProps)
}
