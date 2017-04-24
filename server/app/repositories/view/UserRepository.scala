package repositories.view

import models.UserId
import repositories.eventsource.users.domain.{BasicAuth, User}

import scala.concurrent.Future

trait UserRepository {

  def saveUser(u: User): Future[Unit]

  def saveCredentials(id: UserId, auth: BasicAuth): Future[Unit]

  def fetchUserById(userId: UserId): Future[Option[User]]

  def findUserIdByEmail(email: String): Future[Option[UserId]]

  def credentialsByEmail(email: String): Future[Option[BasicAuth]]
}
