package serenity.users

import javax.inject.{Inject, Named}

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import serenity.users.UserReadProtocol._
import serenity.users.UserWriteProtocol.UpdateCredentialsCmd
import serenity.users.domain.{BasicAuth, User}

import scala.concurrent.Future
import scala.concurrent.duration.DurationDouble
import scala.util.control.NonFatal

class UserService @Inject()(@Named("UserManagerActor") userManagerActor: ActorRef) {

  implicit val timeout: Timeout = 120.seconds

  val logger = Logger(classOf[UserService])

  def findUser(email: String): Future[Option[User]] =
    (userManagerActor ? GetUserWithEmail(email)).map {
      case UserResponse(usr) =>
        logger.debug(s"Found user matching email $email -> ${usr.uuid}")
        Some(usr)

      case _ =>
        logger.debug(s"No matching user for $email")
        None

    }.recover { case NonFatal(t) =>
      logger.warn(s"Unable to retrieve user for email $email", t)
      None
    }

  def findAuth(email: String): Future[Option[BasicAuth]] = {
    (userManagerActor ? GetUserCredentials(email)).map {
      case UserCredentialsResponse(auth) =>
        logger.debug(s"Found credentials for user with email $email")
        Some(auth)

      case CredentialsNotFound =>
        logger.debug(s"No credentials found for user with $email")
        None

      case m =>
        logger.warn(s"No credentials found for user with $email." +
            s" Found an unexpected response of type ${m.getClass}")
        None
    }
  }

  def updateCredentials(email: String, hashedPassword: String): Future[BasicAuth] = {
    (userManagerActor ? UpdateCredentialsCmd(email, hashedPassword)).map {
      case UserCredentialsResponse(auth) =>
        logger.debug(s"Updated credentials for user with email $email")
        auth
    }
  }

}
