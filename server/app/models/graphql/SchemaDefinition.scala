package models.graphql

import models.Types
import sangria.macros.derive
import sangria.macros.derive.ExcludeFields
import sangria.schema._
import models.user.Memberships.{EventbriteMeta, Membership, MembershipIssuer}
import models.user.{Email, User, UserId}
import sangria.validation.{ValueCoercionViolation, Violation}

case class Context(
    user: Option[User]
)

object SchemaDefinition extends Types {

  case object UserIdCoercionViolation
      extends ValueCoercionViolation("UserId value expected")

  implicit val UserIdType = ScalarAlias[UserId, String](
    StringType,
    _.asString,
    id => UserId.fromString(id).toRight(UserIdCoercionViolation)
  )

  implicit val MembershipIssuerType = derive.deriveEnumType[MembershipIssuer.Issuer]()
  implicit val EventbriteMetaType   = derive.deriveObjectType[Unit, EventbriteMeta]()
  implicit val MembershipType = derive.deriveObjectType[Unit, Membership](
    ExcludeFields("issuer")
  )
  implicit val EmailType = derive.deriveObjectType[Unit, Email]()
  implicit val UserType = derive.deriveObjectType[Option[User], User](
    ExcludeFields("roles", "memberships")
  )

  val QueryType = {
    ObjectType(
      "Query",
      fields[Context, Unit](
        Field(
          "me",
          OptionType(UserType),
          resolve = _.ctx.user
        )
      )
    )
  }

  val SerenitySchema = Schema(QueryType)

}
