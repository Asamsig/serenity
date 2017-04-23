package repositories.eventsource.users

import models.Query
import repositories.eventsource.users.domain.{BasicAuth, User, UserId}

object UserReadProtocol {

  case class GetUser(id: UserId) extends Query

  case class GetUserWithEmail(value: String) extends Query

  case class GetUserCredentials(email: String) extends Query

  case class UserCredentialsResponse(user: BasicAuth) extends Query

  case class UpdateView(id: UserId) extends Query

  case class UserResponse(user: User)

  case object UserNotFound

  case object CredentialsNotFound

}
