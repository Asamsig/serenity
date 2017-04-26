package models.user

object Auths {

  sealed trait BasicAuth {
    def password: String
    def salt: Option[String]
  }

  case class HospesAuth(password: String, salt: Option[String]) extends BasicAuth

  case class SerenityAuth(
      password: String
  ) extends BasicAuth {
    val salt: Option[String] = None
  }

}
