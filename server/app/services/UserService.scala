package services

import javax.inject.Inject

import models.user.Auths.{BasicAuth, SerenityAuth}
import models.user.User
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import repositories.view.UserRepository

import scala.concurrent.Future

class UserService @Inject()(userRepository: UserRepository) {

  val logger = Logger(classOf[UserService])

  def findUser(email: String): Future[Option[User]] = {
    userRepository.findUserIdByEmail(email)
        .flatMap {
          case Some(uid) => userRepository.fetchUserById(uid)
          case None => Future.successful(None)
        }
  }

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

}
