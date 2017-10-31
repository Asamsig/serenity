package repositories.view.memory

import models.user.Auths.BasicAuth
import models.user.{User, UserId}
import repositories.view.UserRepository

import scala.concurrent.Future

import scala.concurrent.ExecutionContext.Implicits.global

class MemoryUserRepository extends UserRepository {
  var users: Map[UserId, User]      = Map.empty
  var auths: Map[UserId, BasicAuth] = Map.empty

  override def saveUser(u: User): Future[Unit] = Future {
    users = users + (u.userId -> u)
  }

  override def saveCredentials(id: UserId, auth: BasicAuth): Future[Unit] = Future {
    auths = auths + (id -> auth)
  }

  override def countUsers(): Future[Int] =
    Future(users.size)

  override def fetchUserById(userId: UserId): Future[Option[User]] =
    Future(users.get(userId))

  override def findUserIdByEmail(email: String): Future[Option[UserId]] =
    Future(users.find(_._2.emails.map(_.address).contains(email)).map(_._1))

  override def findUsersIdByEmail(email: Seq[String]): Future[Seq[UserId]] =
    Future {
      users.find {
        case (id, u) =>
          val userEmails = u.emails.map(_.address)
          userEmails.exists(email.contains(_))
      }.map(_._1).toSeq
    }

  override def credentialsByEmail(email: String): Future[Option[BasicAuth]] =
    findUserIdByEmail(email).map {
      case Some(id) => auths.get(id)
      case None     => None
    }

}
