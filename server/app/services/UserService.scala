package services

import javax.inject.{Inject, Named}

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import models.user.Auths.{BasicAuth, SerenityAuth}
import models.user.{User, UserId}
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import repositories.eventsource.users.UserWriteProtocol.UpdateUserProfileCmd
import repositories.view.UserRepository

import scala.concurrent.Future
import scala.concurrent.duration.DurationDouble

class UserService @Inject()(
    userRepository: UserRepository,
    @Named("UserManagerActor") userManagerActor: ActorRef
) {
  implicit val timeout: Timeout = 120.seconds

  val logger = Logger(classOf[UserService])

  def findUser(email: String): Future[Option[User]] = {
    userRepository.findUserIdByEmail(email).flatMap {
      case Some(uid) => userRepository.fetchUserById(uid)
      case None      => Future.successful(None)
    }
  }

  def findUserById(userId: UserId): Future[Option[User]] =
    userRepository.fetchUserById(userId)

  def findAuth(email: String): Future[Option[BasicAuth]] = {
    logger.info(s"requesting cred for user $email ")
    userRepository.credentialsByEmail(email)
  }

  def updateCredentials(email: String, hashedPassword: String): Future[BasicAuth] = {
    userRepository.findUserIdByEmail(email).flatMap {
      case Some(uid) =>
        val auth = SerenityAuth(hashedPassword)
        userRepository.saveCredentials(uid, auth).map(_ => auth)
      case None =>
        Future.failed(new IllegalStateException(s"No user with the give email $email"))
    }
  }

  def updateUser(
      userId: UserId,
      firstName: Option[String],
      lastName: Option[String],
      address: Option[String],
      phone: Option[String]
  ): Future[Option[User]] = {
    findUserById(userId).flatMap {
      case Some(usr) =>
        (for {
          fn    <- firstName.orElse(usr.firstName)
          ln    <- lastName.orElse(usr.lastName)
          adr   <- address.orElse(usr.address)
          phone <- phone.orElse(usr.phone)
        } yield UpdateUserProfileCmd(userId, fn, ln, adr, phone)) match {
          case Some(cmd) => (userManagerActor ? cmd).flatMap(_ => findUserById(userId))
          case None      => Future.failed(new IllegalArgumentException("Invalid input"))
        }

      case None => Future.failed(new IllegalArgumentException("User doesn't exists"))
    }
  }

}
