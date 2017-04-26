package models

import java.time._
import java.time.format.DateTimeParseException

import play.api.libs.json._

class LocalDateTimeFormat extends Format[LocalDateTime] {

  override def reads(json: JsValue): JsResult[LocalDateTime] = {
    try {
      json
        .validate[String]
        .map(v => {
          OffsetDateTime.parse(v).atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime
        })
    } catch {
      case e: DateTimeParseException =>
        JsError("not a valid datetime format with timezone")
    }
  }

  override def writes(o: LocalDateTime): JsValue =
    JsString(o.atOffset(ZoneOffset.UTC).toString)

}

object LocalDateTimeFormatImplicits {
  implicit val localDateTimeFormat = new LocalDateTimeFormat
}
