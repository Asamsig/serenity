package services.graphql

import models.user.Memberships.{EventbriteMeta, Membership, MembershipIssuer}
import models.user.Roles.{AdminRole, Role}
import models.user.{Email, User, UserId}
import sangria.macros.derive
import sangria.macros.derive.ReplaceField
import sangria.schema._
import sangria.validation.ValueCoercionViolation
import services.graphql.Permissions.{IsSelf, WithRole}

import scala.concurrent.ExecutionContext.Implicits.global

object SchemaDefinition {

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

  val UserIdArgument = Argument(
    name = "userId",
    argumentType = OptionInputType(UserIdType)
  )

  val QueryType = {
    ObjectType(
      "Query",
      fields[GraphQlContext, Unit](
        Field(
          "user",
          OptionType(UserType),
          arguments = UserIdArgument :: Nil,
          tags = IsSelf :: WithRole(Seq(AdminRole)) :: Nil,
          resolve = ctx => {
            val userId = ctx.argOpt[UserId]("userId").getOrElse(ctx.ctx.user.get.userId)
            ctx.ctx.userService.findUserById(userId)
          }
        )
      )
    )
  }

  val FirstNameArgument =
    Argument(name = "firstName", argumentType = OptionInputType(StringType))

  val LastNameArgument =
    Argument(name = "lastName", argumentType = OptionInputType(StringType))

  val PhoneArgument =
    Argument(name = "phone", argumentType = OptionInputType(StringType))

  val AddressArgument =
    Argument(name = "address", argumentType = OptionInputType(StringType))

  val MutationType = ObjectType(
    "Mutations",
    fields[GraphQlContext, Unit](
      Field(
        name = "updateUser",
        fieldType = OptionType(UserType),
        tags = IsSelf :: WithRole(Seq(AdminRole)) :: Nil,
        arguments = UserIdArgument :: FirstNameArgument :: LastNameArgument :: PhoneArgument :: AddressArgument :: Nil,
        resolve = ctx => {
          val userId = ctx.argOpt[UserId]("userId").getOrElse(ctx.ctx.user.get.userId)
          ctx.ctx.userService
            .updateUser(
              userId,
              ctx.arg(FirstNameArgument),
              ctx.arg(LastNameArgument),
              ctx.arg(PhoneArgument),
              ctx.arg(AddressArgument)
            )
            .map(u => {
              println(s"Ret user $u")
              u
            })
        }
      )
    )
  )

  val SerenitySchema = Schema(QueryType, Some(MutationType))

}
