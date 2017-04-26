package models

import java.time.{LocalDate, LocalDateTime, OffsetDateTime, ZoneOffset}
import java.util.UUID

import sangria.ast
import sangria.marshalling.MarshallerCapability
import sangria.schema.ScalarType
import sangria.validation.ValueCoercionViolation

import scala.util.{Failure, Success, Try}

trait Types {

  case object UUIDCoercionViolation extends ValueCoercionViolation("UUID value expected")

  case object LocalDateTimeCoercionViolation
      extends ValueCoercionViolation("DateTime with time zone expected")

  case object LocalDateCoercionViolation extends ValueCoercionViolation("Date expected")

  implicit val UUIDType: ScalarType[UUID] = ScalarType[UUID](
    "UUID",
    description = Some("The `UUID` scalar type represents a unique identifier"),
    coerceOutput =
      (value: UUID, capabilities: Set[MarshallerCapability]) => value.toString,
    coerceUserInput = {
      case s: String ⇒ Right(UUID.fromString(s))
      case u: UUID   ⇒ Right(u)
      case _         ⇒ Left(UUIDCoercionViolation)
    },
    coerceInput = {
      case ast.StringValue(s, _, _) ⇒
        toValue(() => UUID.fromString(s), UUIDCoercionViolation)
      case _ ⇒ Left(UUIDCoercionViolation)
    }
  )

  implicit val LocalDateTimeType: ScalarType[LocalDateTime] = ScalarType[LocalDateTime](
    "LocalDateTime",
    description = Some("The `LocalDateTime` scalar type represents a date time"),
    coerceOutput = (value: LocalDateTime, capabilities: Set[MarshallerCapability]) =>
      value.atOffset(ZoneOffset.UTC).toString,
    coerceUserInput = {
      case s: String ⇒
        Right(OffsetDateTime.parse(s).atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime)
      case u: LocalDateTime ⇒ Right(u)
      case _                ⇒ Left(LocalDateTimeCoercionViolation)
    },
    coerceInput = {
      case ast.StringValue(s, _, _) ⇒
        toValue(
          () =>
            OffsetDateTime.parse(s).atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime,
          LocalDateTimeCoercionViolation
        )
      case _ ⇒ Left(LocalDateTimeCoercionViolation)
    }
  )

  implicit val LocalDateType: ScalarType[LocalDate] = ScalarType[LocalDate](
    "LocalDate",
    description = Some("The `LocalDate` scalar type represents a date"),
    coerceOutput =
      (value: LocalDate, capabilities: Set[MarshallerCapability]) => value.toString,
    coerceUserInput = {
      case s: String    ⇒ Right(LocalDate.parse(s))
      case u: LocalDate ⇒ Right(u)
      case _            ⇒ Left(LocalDateTimeCoercionViolation)
    },
    coerceInput = {
      case ast.StringValue(s, _, _) ⇒
        toValue(
          () => OffsetDateTime.parse(s).atZoneSameInstant(ZoneOffset.UTC).toLocalDate,
          LocalDateTimeCoercionViolation
        )
      case _ ⇒ Left(LocalDateTimeCoercionViolation)
    }
  )

  private def toValue[T, V >: ValueCoercionViolation](
      f: () => T,
      violation: V
  ): Either[V, T] = {
    Try(f()) match {
      case Success(v) => Right(v)
      case Failure(t) => Left(violation)
    }
  }

}
