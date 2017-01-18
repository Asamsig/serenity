package serenity.users

import java.security.MessageDigest
import java.util.Base64

import serenity.users.domain.BasicAuth

import scala.util.Random

object HospesPassword {

  private lazy val encoder = Base64.getEncoder

  def validate(basicAuth: BasicAuth, password: String): Boolean =
    hash(password, basicAuth.salt) == basicAuth.password

  def createPasswordAndSalt(plainPassword: String): (String, String) = {
    val slt = Random.alphanumeric.take(16).mkString
    val pwd = new String(hash(plainPassword, slt))
    (pwd, slt)
  }

  private def hash(password: String, salt: String) =
    new String(encoder.encode(HospesPassword.sha(s"{$password} salt={$salt}")))

  private def sha(in: String) =
    MessageDigest.getInstance("SHA").digest(in.getBytes("UTF-8"))

}