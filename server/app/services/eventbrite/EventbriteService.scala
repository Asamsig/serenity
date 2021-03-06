package services.eventbrite

import javax.inject.{Inject, Named}

import akka.actor.ActorRef
import akka.util.Timeout
import models.EventbriteHandleStatus.{Failure, NotSupported, Success}
import models.{EventbriteHandleStatus, EventbriteWebHook}
import play.api.Logger
import repositories.eventsource.users.UserWriteProtocol.CreateOrUpdateUserCmd

import scala.concurrent.duration.DurationDouble
import scala.concurrent.{ExecutionContext, Future}

class EventbriteService @Inject()(
    client: EventbriteClient,
    @Named("UserManagerActor") userManagerActor: ActorRef
)(implicit ec: ExecutionContext) {

  implicit val timeout: Timeout = 120.seconds
  type Status = EventbriteHandleStatus.Status
  val logger = Logger(getClass)

  def handleWebHook(webhook: EventbriteWebHook): Future[Status] =
    webhook.details.action match {
      case "test" =>
        logger.debug("WebHook test ok!")
        Future.successful(Success)
      case "attendee.updated" =>
        val result = client
          .attendee(webhook.details.apiUrl, webhook.store)
          .map(attendee => {
            userManagerActor ! CreateOrUpdateUserCmd(attendee)
            logger.debug(s"Found attendee $attendee")
            Success
          })
        result.recover {
          case t =>
            logger.warn(s"Failed to process webhook $webhook", t)
            Failure
        }
      case _ =>
        logger.info(s"No matching webhook handler for action: ${webhook.details.action}")
        Future.successful(NotSupported)
    }

}
