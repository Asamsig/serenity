package auth

import com.mohiva.play.silhouette.api.Authorization
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import models.user.Roles.Role
import models.user.User
import play.api.mvc.Request

import scala.concurrent.Future

case class WithRole(roles: Role*) extends Authorization[User, JWTAuthenticator] {

  override def isAuthorized[B](identity: User, authenticator: JWTAuthenticator)(
      implicit request: Request[B]
  ): Future[Boolean] =
    Future.successful(identity.roles.exists(roles.contains))

}
