package repositories.view

import models.user.Auths.BasicAuth
import models.user.{User, UserId}

import scala.concurrent.Future

trait UserRepository {

  def saveUser(u: User): Future[Unit]

  def saveCredentials(id: UserId, auth: BasicAuth): Future[Unit]

  def countUsers(): Future[Int]

  def fetchUserById(userId: UserId): Future[Option[User]]

  def findUserIdByEmail(email: String): Future[Option[UserId]]

  def findUsersIdByEmail(email: Seq[String]): Future[Seq[UserId]]

  def credentialsByEmail(email: String): Future[Option[BasicAuth]]
}
