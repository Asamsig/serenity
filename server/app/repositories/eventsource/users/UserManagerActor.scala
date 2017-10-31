package repositories.eventsource.users

import akka.actor.{Actor, ActorRef, Props}
import models.user.UserId
import repositories.eventsource.users.UserReadProtocol._
import repositories.eventsource.users.UserWriteProtocol._
import repositories.view.UserRepository

import scala.util.Failure

class UserManagerActor(repo: UserRepository, userActorProps: UserId => Props)
    extends Actor {

  implicit val ec = context.system.dispatcher

  override def receive: Receive = commands orElse query

  def getOrCreateUserActor(u: UserId): ActorRef = {
    val name = s"user-${u.asString}"
    context.child(name).getOrElse(context.actorOf(userActorProps(u), name))
  }

  def commands: Receive = {
    case cmd: CreateOrUpdateUserCmd =>
      val respondTo = sender()
      repo.findUserIdByEmail(cmd.attendee.profile.email).foreach {
        case Some(uid) => getOrCreateUserActor(uid).tell(cmd, respondTo)
        case None      => getOrCreateUserActor(UserId.generate()).tell(cmd, respondTo)
      }

    case cmd: UpdateUserProfileCmd =>
      val respondTo = sender()
      repo.fetchUserById(cmd.userId).foreach {
        case Some(usr) => getOrCreateUserActor(usr.userId).tell(cmd, respondTo)
        case None      => respondTo ! Failure(ValidationFailed("User doesn't exist"))
      }

    case cmd: HospesImportCmd =>
      val respondTo = sender()
      repo.findUsersIdByEmail(cmd.user.email.map(_.address)).foreach {
        case Nil => getOrCreateUserActor(UserId.generate()).tell(cmd, respondTo)
        case _   => respondTo ! Failure(ValidationFailed("User exist"))
      }

    case cmd: UpdateCredentialsCmd =>
      val respondTo = sender()
      repo.findUserIdByEmail(cmd.email).foreach {
        case Some(uid) => getOrCreateUserActor(uid).tell(cmd, respondTo)
        case None      => respondTo ! Failure(ValidationFailed("User does not exist"))
      }
  }

  def query: Receive = {
    case GetUserWithEmail(email) =>
      val respondTo = sender()
      repo.findUserIdByEmail(email).foreach {
        case Some(uid) => getOrCreateUserActor(uid).tell(GetUser(uid), respondTo)
        case None =>
          respondTo ! Failure(ValidationFailed("User with email doesn't exist"))
      }

    case qry @ GetUserCredentials(email) =>
      val respondTo = sender()
      repo.findUserIdByEmail(email).foreach {
        case Some(uid) => getOrCreateUserActor(uid).tell(qry, respondTo)
        case None      => respondTo ! CredentialsNotFound
      }

    case qry @ UpdateView(uid) =>
      getOrCreateUserActor(uid).forward(qry)
  }

}

object UserManagerActor {
  def apply(
      repo: UserRepository,
      userActorProps: (UserRepository, UserId) => Props = UserActor.apply
  ): Props = {
    val curried: UserId => Props = userActorProps.curried(repo)
    Props(classOf[UserManagerActor], repo: UserRepository, curried)
  }
}
