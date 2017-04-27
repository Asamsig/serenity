package models.graphql

import models.Types
import models.user.Memberships.{EventbriteMeta, Membership, MembershipIssuer}
import models.user.Roles.Role
import models.user.{Email, User, UserId}
import sangria.macros.derive
import sangria.macros.derive.ReplaceField
import sangria.schema._
import sangria.validation.ValueCoercionViolation

case class Context(
    user: Option[User]
)

object SchemaDefinition extends Types {

  case object UserIdCoercionViolation
      extends ValueCoercionViolation("UserId value expected")

  case object ReadValueCoercionViolation
      extends ValueCoercionViolation("Reading this value isn't supported")

  implicit val UserIdType = ScalarAlias[UserId, String](
    StringType,
    _.asString,
    id => UserId.fromString(id).toRight(UserIdCoercionViolation)
  )

  implicit val RolesType = ScalarAlias[Role, String](
    StringType,
    _.name,
    r => Right(Role.apply(r))
  )

  implicit val MembershipIssuerType = ScalarAlias[MembershipIssuer.Issuer, String](
    StringType,
    _.toString,
    _ => Left(ReadValueCoercionViolation)
  )

  implicit val EventbriteMetaType = derive.deriveObjectType[Unit, EventbriteMeta]()
  implicit val MembershipType     = derive.deriveObjectType[Unit, Membership]()
  implicit val EmailType          = derive.deriveObjectType[Unit, Email]()
  implicit val UserType = derive.deriveObjectType[Option[User], User](
    ReplaceField(
      "roles",
      Field("roles", ListType(RolesType), resolve = _.value.roles.toSeq)
    ),
    ReplaceField(
      "memberships",
      Field("memberships", ListType(MembershipType), resolve = _.value.memberships.toSeq)
    )
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
