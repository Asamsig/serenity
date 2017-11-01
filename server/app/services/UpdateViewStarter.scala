package services

import javax.inject.{Inject, Singleton}

import play.api.Logger
import repositories.view.UserRepository

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

@Singleton
class UpdateViewStarter @Inject()(
    userRepository: UserRepository,
    updateUserViewService: UpdateUserViewService
)(implicit ec: ExecutionContext) {

  val logger = Logger(classOf[UpdateViewStarter])

  logger.info("Starting updating user view")

  userRepository
    .countUsers()
    .flatMap {
      case 0 =>
        updateUserViewService.updateAll().flatMap { _ =>
          userRepository
            .countUsers()
            .map(count => s"Done updating user view with $count users")
        }
      case _ => Future.successful("Didn't update user view, not needed")
    }
    .recover {
      case NonFatal(t) =>
        logger.warn("Failed to run update user view", t)
        s"Failed to run update user view ${t.getMessage}"
    }
    .foreach(s => logger.info(s))

}
