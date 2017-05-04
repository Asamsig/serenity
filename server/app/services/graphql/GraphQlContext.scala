package services.graphql

import models.user.User
import repositories.view.UserRepository

case class GraphQlContext(
    user: Option[User],
    userRepository: UserRepository
)
