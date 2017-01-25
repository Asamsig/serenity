package serenity.users

import javax.inject.{Inject, Named}

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import serenity.users.UserReadProtocol._
import serenity.users.domain.{BasicAuth, User}

import scala.concurrent.duration.DurationDouble
import scala.concurrent.{ExecutionContext, Future}

class UserService @Inject()(@Named("UserManagerActor") userManagerActor: ActorRef)(implicit ec: ExecutionContext) {

  implicit val timeout: Timeout = 120.seconds

  def findUser(email: String): Future[Option[User]] =
    (userManagerActor ? GetUserWithEmail(email)).map{
      case UserResponse(usr) => Some(usr)
      case _ => None
    }.recover{case _ => None}

  def findAuth(email: String): Future[BasicAuth] = {
    (userManagerActor ? GetUserCredentials(email)).map {
      case UserCredentialsResponse(auth) => auth
      case CredentialsNotFound => throw new IllegalStateException("CredentialsNotFound")
      case m => throw new IllegalStateException(s"Unknown fault $m")
    }
  }

}
