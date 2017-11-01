package auth

import javax.inject.Inject

import com.google.inject.name.Named
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.{PasswordHasher, PasswordInfo}
import com.mohiva.play.silhouette.password.BCryptPasswordHasher
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import models.user.Auths.{HospesAuth, SerenityAuth}
import services.UserService

import scala.concurrent.{ExecutionContext, Future}

class PasswordAuth @Inject()(
    userService: UserService,
    adminUser: AdminUser,
    @Named("bcryptHasher") bcryptHasher: PasswordHasher
)(implicit ec: ExecutionContext)
  extends DelegableAuthInfoDAO[PasswordInfo] {

  override def find(loginInfo: LoginInfo): Future[Option[PasswordInfo]] = {
    if (loginInfo.providerKey == adminUser.user.mainEmail.address) {
      Future.successful(Some(adminUser.auth))
    } else {
      userService
        .findAuth(loginInfo.providerKey)
        .map(_.flatMap {
          case SerenityAuth(pwd) =>
            Some(PasswordInfo(BCryptPasswordHasher.ID, pwd, None))

          case HospesAuth(pwd, salt) =>
            Some(PasswordInfo(HospesPasswordHasher.ID, pwd, salt))
        })
        .recover {
          case _ => None
        }
    }
  }

  override def add(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] =
    save(loginInfo, authInfo)

  override def update(
      loginInfo: LoginInfo,
      authInfo: PasswordInfo
  ): Future[PasswordInfo] =
    save(loginInfo, authInfo)

  override def save(
      loginInfo: LoginInfo,
      passwordInfo: PasswordInfo
  ): Future[PasswordInfo] = {
    passwordInfo.hasher match {
      case BCryptPasswordHasher.ID =>
        val future = userService
          .updateCredentials(loginInfo.providerKey, passwordInfo.password)
          .map(t => PasswordInfo(BCryptPasswordHasher.ID, t.password))
        future

      case id => throw new IllegalStateException(s"Hash type not supported $id")
    }
  }

  override def remove(loginInfo: LoginInfo): Future[Unit] = ???

}
