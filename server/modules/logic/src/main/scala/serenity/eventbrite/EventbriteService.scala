package serenity.eventbrite

import javax.inject.Inject

import serenity.eventbrite.EventbriteHandleStatus.Success

import scala.concurrent.{ExecutionContext, Future}

class EventbriteService @Inject()(implicit ec: ExecutionContext) {

  type Status = EventbriteHandleStatus.Status

  def handleWebHook(webhook: EventbriteWebHook): Future[Status] =
    Future {
      println(s"handling webhook: $webhook")
      Success
    }

}
