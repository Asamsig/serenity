package auth

import com.mohiva.play.silhouette.api.Authorization
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import play.api.mvc.Request
import serenity.users.domain.{Role, User}

import scala.concurrent.Future

case class WithRole(roles: Role*) extends Authorization[User, JWTAuthenticator] {

  override def isAuthorized[B](identity: User, authenticator: JWTAuthenticator)
      (implicit request: Request[B]): Future[Boolean] =
    Future.successful(identity.roles.exists(roles.contains))

}
