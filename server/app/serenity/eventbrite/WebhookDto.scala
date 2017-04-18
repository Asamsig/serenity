package serenity.eventbrite

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}

object EventbriteStore extends Enumeration {
  type Store = Value
  val javaBin = Value("javabin")
  val javaZone = Value("javazone")
}

case class WebHookDetails(
    action: String,
    userId: Option[String],
    webHookUrl: String,
    webhookId: String,
    apiUrl: String
)

case class EventbriteWebHook(
    store: EventbriteStore.Store,
    details: WebHookDetails
)

trait WebHookInfoJson {
  implicit val webHookInfoReader: Reads[WebHookDetails] = (
      (JsPath \ "config" \ "action").read[String] and
          (JsPath \ "config" \ "user_id").readNullable[String] and
          (JsPath \ "config" \ "endpoint_url").read[String] and
          (JsPath \ "config" \ "webhook_id").read[String] and
          (JsPath \ "api_url").read[String]
      ) (WebHookDetails.apply _)
}

object EventbriteHandleStatus extends Enumeration {
  type Status = Value
  val Success = Value
  val Failure = Value
  val NotSupported = Value
}