package auth

import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService
import serenity.users.UserService
import serenity.users.domain.User

import scala.concurrent.Future

class UserIdentityService @Inject()(userService: UserService, adminUser: AdminUser) extends IdentityService[User] {

  override def retrieve(loginInfo: LoginInfo): Future[Option[User]] =
    if (loginInfo.providerKey == adminUser.user.mainEmail.address)
      Future.successful(Some(adminUser.user))
    else
      userService.findUser(loginInfo.providerKey)

}
