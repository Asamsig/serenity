package auth

import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.password.BCryptPasswordHasher
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import serenity.users.UserService
import serenity.users.domain.{HospesAuth, SerenityAuth}

import scala.concurrent.{ExecutionContext, Future}

class PasswordAuth @Inject()(userService: UserService, adminUser: AdminUser)(implicit ec: ExecutionContext)
    extends DelegableAuthInfoDAO[PasswordInfo] {

  override def find(loginInfo: LoginInfo): Future[Option[PasswordInfo]] = {
    if (loginInfo.providerKey == adminUser.user.mainEmail.address)
      Future.successful(Some(adminUser.auth))
    else userService.findAuth(loginInfo.providerKey).map {
      case Some(auth) => auth match {

        case SerenityAuth(pwd, salt) =>
          Some(PasswordInfo(BCryptPasswordHasher.ID, pwd, Some(salt)))

        case HospesAuth(pwd, salt) =>
          Some(PasswordInfo(HospesPasswordHasher.ID, pwd, Some(salt)))

        case _ =>
          None
      }

      case None =>
        None

    }.recover {
      case _ => None
    }
  }

  override def add(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = ???

  override def update(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = ???

  override def save(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = ???

  override def remove(loginInfo: LoginInfo): Future[Unit] = ???

}
