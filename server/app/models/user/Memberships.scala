package models.user

import java.time.LocalDate

object Memberships {

  case class Membership(
      from: LocalDate,
      to: LocalDate,
      issuer: MembershipIssuer.Issuer,
      eventbriteMeta: Option[EventbriteMeta]
  )

  case class EventbriteMeta(
      attendeeId: String,
      eventId: String,
      orderId: String
  )

  object MembershipIssuer extends Enumeration {
    type Issuer = Value
    val JavaBin  = Value
    val JavaZone = Value

    def toInt(i: Issuer): Int = i match {
      case JavaBin  => 1
      case JavaZone => 2
    }

    def unsafeFromInt(i: Int): Issuer = i match {
      case 1 => JavaBin
      case 2 => JavaZone
    }
  }
}
