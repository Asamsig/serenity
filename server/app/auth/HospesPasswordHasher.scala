package auth

import java.security.MessageDigest
import java.util.Base64

import auth.HospesPasswordHasher.ID
import com.mohiva.play.silhouette.api.util.{PasswordHasher, PasswordInfo}

import scala.util.Random

class HospesPasswordHasher extends PasswordHasher {
  private val saltLength   = 16
  private lazy val encoder = Base64.getEncoder
  private lazy val digest  = MessageDigest.getInstance("SHA")
  override def id: String  = ID

  override def hash(plainPassword: String): PasswordInfo = {

    val salt = Random.alphanumeric.take(saltLength).mkString
    val pwd  = sha(plainPassword, salt)
    PasswordInfo(ID, pwd, Some(salt))
  }

  private def sha(plainPassword: String, salt: String) =
    new String(
      encoder.encode(digest.digest(s"{$plainPassword} salt={$salt}".getBytes("UTF-8")))
    )

  override def matches(passwordInfo: PasswordInfo, suppliedPassword: String): Boolean =
    sha(suppliedPassword, passwordInfo.salt.get) == passwordInfo.password

  override def isDeprecated(passwordInfo: PasswordInfo): Option[Boolean] =
    Some(true)

}

object HospesPasswordHasher {

  val ID = "hospes"

}
