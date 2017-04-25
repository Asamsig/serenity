package models

import java.time.{LocalDate, LocalDateTime, LocalTime}

import org.scalatest.{FunSpec, Matchers}
import play.api.libs.json.JsString

class LocalDateTimeFormatSpec extends FunSpec with Matchers {

  private val format = new LocalDateTimeFormat()

  describe("reads") {
    val expected = LocalDateTime.of(LocalDate.of(2010, 1, 31), LocalTime.of(13, 1, 2, 0))
    it("should parse ISO-8601 with timezone Z") {
      val actual = format.reads(JsString("2010-01-31T13:01:02Z"))
      actual.get should be(expected)
    }

    it("should parse ISO-8601 with timezone +1") {
      val actual = format.reads(JsString("2010-01-31T14:01:02+01:00"))
      actual.get should be(expected)
    }

    it("should not parse date time without timezone") {
      val actual = format.reads(JsString("2010-01-31T13:01:02"))
      actual.asOpt should be(None)
    }
  }

  describe("writes") {
    val input = LocalDateTime.of(LocalDate.of(2010, 1, 31), LocalTime.of(13, 1, 2, 0))
    it("should write to utc time") {
      val actual = format.writes(input)

      actual should equal(JsString("2010-01-31T13:01:02Z"))
    }
  }
}
