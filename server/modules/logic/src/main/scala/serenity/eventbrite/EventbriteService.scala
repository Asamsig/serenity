package serenity.eventbrite

import javax.inject.Inject

import play.api.Logger
import serenity.eventbrite.EventbriteHandleStatus.{Failure, NotSupported, Success}

import scala.concurrent.{ExecutionContext, Future}

class EventbriteService @Inject()(client: EventbriteClient)(implicit ec: ExecutionContext) {

  type Status = EventbriteHandleStatus.Status
  val logger = Logger(getClass)

  def handleWebHook(webhook: EventbriteWebHook): Future[Status] =
    webhook.details.action match {
      case "test" =>
        Future.successful(Success)
      case "attendee.updated" =>
        val result = client.attendee(webhook.details.apiUrl)
            .map(attendee => {
              //todo create command
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
