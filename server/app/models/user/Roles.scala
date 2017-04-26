package models.user

object Roles {

  sealed trait Role {
    def name: String
  }

  object Role {
    def apply(role: String) = role match {
      case AdminRole.name => AdminRole
      case _              => UnknownRole
    }
  }

  case object AdminRole extends Role {
    val name = "admin"
  }

  case object UnknownRole extends Role {
    val name = "-"
  }
}
