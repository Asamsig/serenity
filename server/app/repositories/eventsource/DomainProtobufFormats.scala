package repositories.eventsource

import java.time.{Instant, LocalDate, LocalDateTime, ZoneOffset}
import java.util.UUID

import com.google.protobuf.Message
import com.google.protobuf.timestamp.Timestamp
import models.EventMeta
import models.user.Memberships.{EventbriteMeta, MembershipIssuer}
import models.user.{Email, UserId}
import repositories.eventsource.protobuf.ProtobufFormat
import serenity.protobuf.Userevents
import serenity.protobuf.Userevents.BasicAuthMessage.AuthSourceEnum
import serenity.protobuf.userevents.MembershipUpdateMessage.{ActionEnum, EventbriteInformation, IssuerEnum}
import serenity.protobuf.userevents._
import serenity.protobuf.uuid.{UUID => PUUID}
import users.UserWriteProtocol._

import scala.language.implicitConversions

object DomainProtobufFormats {
  implicit def javaUuidToProtoUuid(id: UUID): Option[PUUID] =
    Some(PUUID(id.getMostSignificantBits, id.getLeastSignificantBits))

  implicit def protoUuidToJavaUuid(id: Option[PUUID]): UUID = id match {
    case Some(v) => new UUID(v.msb, v.lsb)
    case None => throw new IllegalStateException("id is required and isn't allowed to be None")
  }

  implicit def strToOptionString(str: String): Option[String] = Option(str)

  implicit def optionStrToString(opt: Option[String]): String = opt.getOrElse("")

  implicit def toEventMeta(m: EventMeta): Option[EventMetaMessage] = {
    val instant = m.created.toInstant(ZoneOffset.UTC)
    Some(EventMetaMessage(Some(Timestamp(instant.getEpochSecond, instant.getNano))))
  }

  implicit def toTimestamp(d: LocalDate): Option[Timestamp] =
    Some(Timestamp(d.atStartOfDay().toInstant(ZoneOffset.UTC).getEpochSecond))

  implicit def fromTimestamp(ts: Option[Timestamp]): LocalDate =
    LocalDateTime.ofInstant(
      Instant.ofEpochSecond(
        ts.get.seconds.toLong,
        ts.get.nanos.toLong),
      ZoneOffset.UTC).toLocalDate

  implicit def fromEventMeta(m: Option[EventMetaMessage]): EventMeta =
    m.map(em => EventMeta(LocalDateTime.ofInstant(
      Instant.ofEpochSecond(
        em.created.get.seconds.toLong,
        em.created.get.nanos.toLong),
      ZoneOffset.UTC))).get

  implicit val basicAuthPBP = new ProtobufFormat[BasicAuthEvt] {
    override def read(proto: Message): BasicAuthEvt = proto match {
      case jm: Userevents.BasicAuthMessage =>
        val m = BasicAuthMessage.fromJavaProto(jm)
        BasicAuthEvt(
          UserId(m.id),
          m.password,
          m.salt,
          m.source.value match {
            case AuthSourceEnum.HOSPES_VALUE => HospesAuthSource
            case AuthSourceEnum.SERENITY_VALUE => SerenityAuthSource
          },
          m.meta
        )
    }

    override def write(e: BasicAuthEvt): Message = {
      BasicAuthMessage.toJavaProto(BasicAuthMessage(
        e.id.underling,
        e.password,
        e.salt,
        e.source match {
          case HospesAuthSource => BasicAuthMessage.AuthSourceEnum.HOSPES
          case SerenityAuthSource => BasicAuthMessage.AuthSourceEnum.SERENITY
        },
        e.meta
      ))
    }
  }

  implicit val hospesUserImportPBP = new ProtobufFormat[HospesUserImportEvt] {
    override def read(proto: Message): HospesUserImportEvt = proto match {
      case jm: Userevents.HospesUserImportMessage =>
        val m = HospesUserImportMessage.fromJavaProto(jm)
        HospesUserImportEvt(
          UserId(m.id),
          m.originId.toList,
          m.emails.map(em => Email(em.address, em.validated)).toList,
          m.firstName,
          m.lastName,
          m.address,
          m.phoneNumber,
          m.meta
        )
    }

    override def write(e: HospesUserImportEvt): Message =
      HospesUserImportMessage.toJavaProto(HospesUserImportMessage(
        e.id.underling,
        e.originId,
        e.email.map(em => EmailMessage(em.address, em.validated)),
        e.firstName,
        e.lastName,
        e.address,
        e.phoneNumber,
        e.meta
      ))
  }

  implicit val userRegisteredPBP = new ProtobufFormat[UserUpdatedEvt] {
    override def read(proto: Message): UserUpdatedEvt = proto match {
      case jm: Userevents.UserUpdatedMessage =>
        val m = UserUpdatedMessage.fromJavaProto(jm)
        UserUpdatedEvt(
          UserId(m.id),
          m.email.map(em => Email(em.address, em.validated)).get.address,
          m.firstName,
          m.lastName,
          m.phone,
          m.meta
        )
    }

    override def write(e: UserUpdatedEvt): Message =
      UserUpdatedMessage.toJavaProto(UserUpdatedMessage(
        e.id.underling,
        Some(EmailMessage(e.email, validated = true)),
        e.firstName,
        e.lastName,
        e.phone,
        e.meta
      ))
  }

  implicit val membershipUpdate = new ProtobufFormat[MembershipUpdateEvt] {

    override def read(proto: Message): MembershipUpdateEvt = proto match {
      case jm: Userevents.MembershipUpdateMessage =>
        val m = MembershipUpdateMessage.fromJavaProto(jm)
        MembershipUpdateEvt(
          m.from,
          m.action match {
            case ActionEnum.ADD => MembershipAction.Add
            case ActionEnum.REMOVE => MembershipAction.Remove
            case e@_ => throw new IllegalArgumentException(s"Unknown enum for ActionEnum. Value: $e")
          },
          m.issuer match {
            case IssuerEnum.JAVA_BIN => MembershipIssuer.JavaBin
            case IssuerEnum.JAVA_ZONE => MembershipIssuer.JavaZone
            case e@_ => throw new IllegalArgumentException(s"Unknown enum for IssuerEnum. Value: $e")
          },
          m.eventbriteInformation.map(ei => EventbriteMeta(ei.attendeeId, ei.eventId, ei.orderId)),
          m.meta
        )
    }

    override def write(e: MembershipUpdateEvt): Message =
      MembershipUpdateMessage.toJavaProto(MembershipUpdateMessage(
        e.from,
        e.from.plusYears(1).minusDays(1),
        e.eventbirteMeta.map(em => EventbriteInformation(em.attendeeId, em.eventId, em.orderId)),
        e.issuer match {
          case MembershipIssuer.JavaBin => IssuerEnum.JAVA_BIN
          case MembershipIssuer.JavaZone => IssuerEnum.JAVA_ZONE
        },
        e.action match {
          case MembershipAction.Add => ActionEnum.ADD
          case MembershipAction.Remove => ActionEnum.REMOVE
        },

        e.meta
      ))
  }

}
