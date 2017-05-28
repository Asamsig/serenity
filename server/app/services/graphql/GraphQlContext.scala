package services.graphql

import models.user.User
import services.UserService

case class GraphQlContext(
    user: Option[User],
    userService: UserService
)
