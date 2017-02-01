package serenity.eventbrite

import java.time.LocalDateTime

import play.api.libs.functional.syntax._
import play.api.libs.json._
import serenity.eventbrite.EventbriteStore.Store

case class Attendee(
    profile: Profile,
    attendeeMeta: AttendeeMeta,
    store: Store
)

case class Profile(
    firstName: String,
    lastName: String,
    phone: String,
    email: String,
    address: Option[AttendeeAddress] = None
)

case class AttendeeMeta(
    id: String,
    orderId: String,
    eventId: String,
    created: LocalDateTime,
    cancelled: Boolean,
    refunded: Boolean
) {
  lazy val status: Status = (cancelled, refunded) match {
    case (false, false) => Update
    case _ => Delete
  }
}

case class AttendeeAddress(
    address_1: Option[String],
    address_2: Option[String],
    city: Option[String],
    region: Option[String],
    postal_code: Option[String],
    country: Option[String]
)

sealed trait Status
case object Update extends Status
case object Delete extends Status

trait EventbriteApiDtoJson {

  import serenity.json.LocalDateTimeFormatImplicits.localDateTimeFormat

  implicit val attendeeAddressJsonReader: Reads[AttendeeAddress] = (
      (JsPath \ "address_1").readNullable[String] and
          (JsPath \ "address_2").readNullable[String] and
          (JsPath \ "city").readNullable[String] and
          (JsPath \ "region").readNullable[String] and
          (JsPath \ "postal_code").readNullable[String] and
          (JsPath \ "country").readNullable[String]
      ) (AttendeeAddress.apply _)

  implicit val profileJsonReader: Reads[Profile] = (
      (JsPath \ "first_name").read[String] and
          (JsPath \ "last_name").read[String] and
          (JsPath \ "cell_phone").read[String] and
          (JsPath \ "email").read[String] and
          (JsPath \ "addresses" \ "home").readNullable[AttendeeAddress]
      ) (Profile.apply _)

  implicit val attendeeMetaJsonReader: Reads[AttendeeMeta] = (
      (JsPath \ "id").read[String] and
          (JsPath \ "order_id").read[String] and
          (JsPath \ "event_id").read[String] and
          (JsPath \ "created").read[LocalDateTime] and
          (JsPath \ "cancelled").read[Boolean] and
          (JsPath \ "refunded").read[Boolean]
      ) (AttendeeMeta.apply _)

}
