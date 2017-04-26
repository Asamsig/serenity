package services.eventbrite

import javax.inject.Inject

import models._
import play.api.Logger
import play.api.http.{Status => HttpStatus}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.ws.WSClient
import models.EventbriteStore.Store

import scala.concurrent.Future

case class EventbriteTokens(javabin: String, javazone: String)

class EventbriteClient @Inject()(
    ws: WSClient,
    tokens: EventbriteTokens
) extends EventbriteApiDtoJson {

  val logger = Logger(classOf[EventbriteClient])

  def attendee(url: String, store: Store): Future[Attendee] = {
    ws.url(url)
      .withQueryString(("token", store match {
        case EventbriteStore.javaBin  => tokens.javabin
        case EventbriteStore.javaZone => tokens.javazone
      }))
      .get()
      .map(
        r =>
          r.status match {
            case HttpStatus.OK =>
              val jsonResponse = r.json
              Attendee(
                (jsonResponse \ "profile").as[Profile],
                jsonResponse.as[AttendeeMeta],
                store
              )
            case code =>
              logger.warn(
                s"Unexpected return code from eventbrite. Code: $code Body: ${r.body}"
              )
              throw new IllegalStateException("Unexpected response from eventbrite")
        }
      )
  }
}
