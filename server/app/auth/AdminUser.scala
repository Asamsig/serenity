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
    mainEmail = Email(envOrConf("email"), validated = true),
    createdDate = UtcDateTime.nowUTC(),
    firstName = Some("admin"),
    lastName = Some("javaBin"),
    roles = Set(AdminRole)
  )

  lazy val auth: PasswordInfo = hasher.hash(envOrConf("password"))

  def envOrConf(path: String): String = {
    val fallback = configuration.getString(s"$basePath.$path")
    envOrSome(s"ADMIN_${path.toUpperCase()}", fallback).get
  }
}
