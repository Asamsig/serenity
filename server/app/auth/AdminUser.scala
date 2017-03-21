package auth

import java.util.UUID
import javax.inject.{Inject, Named}

import com.mohiva.play.silhouette.api.util.{PasswordHasher, PasswordInfo}
import play.api.Configuration
import serenity.UtcDateTime
import serenity.users.domain.{AdminRole, Email, User}

import scala.util.Properties._

class AdminUser @Inject()(@Named("bcryptHasher") hasher: PasswordHasher, configuration: Configuration) {

  private val basePath = "serenity.adminuser"

  lazy val user: User = User(
    uuid = UUID.randomUUID(),
    mainEmail = Email(strFromCfg("email"), validated = true),
    createdDate = UtcDateTime.nowUTC(),
    firstName = Some("admin"),
    lastName = Some("javaBin"),
    roles = Set(AdminRole)
  )

  lazy val auth: PasswordInfo = hasher.hash(strFromCfg("password"))

  def strFromCfg(path: String): String =
    configuration.underlying.getString(s"$basePath.$path")

}
