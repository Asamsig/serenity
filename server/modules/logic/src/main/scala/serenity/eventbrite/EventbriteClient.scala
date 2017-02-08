package serenity.eventbrite

import javax.inject.{Inject, Named}

import play.api.libs.ws.WSClient
import serenity.eventbrite.EventbriteStore.Store

import scala.concurrent.{ExecutionContext, Future}

class EventbriteClient @Inject() (
    ws: WSClient,
    @Named("eventbrite.javabin.token") javaBinToken: String,
    @Named("eventbrite.javazone.token") javaZoneToken: String)
    (implicit ec: ExecutionContext) extends EventbriteApiDtoJson{


  def attendee(url: String, store: Store): Future[Attendee] = {
    ws.url(url)
        .withQueryString(("token", store match {
          case EventbriteStore.javaBin => javaBinToken
          case EventbriteStore.javaZone => javaZoneToken
        }))
        .get().map(r => {
      val jsonResponse = r.json
      Attendee(
        (jsonResponse \ "profile").as[Profile],
        jsonResponse.as[AttendeeMeta],
        store
      )
    })
  }
}
