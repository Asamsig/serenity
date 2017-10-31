package services.graphql

import services.graphql.Permissions.{IsSelf, Permission, WithRole}
import models.user.Roles.Role
import sangria.execution.{
  FieldTag,
  Middleware,
  MiddlewareBeforeField,
  MiddlewareQueryContext
}
import sangria.schema.Context

case object PermissionEnforcerMiddleware
    extends Middleware[GraphQlContext]
    with MiddlewareBeforeField[GraphQlContext] {
  override type QueryVal = Unit
  override type FieldVal = Unit

  override def beforeQuery(context: MiddlewareQueryContext[GraphQlContext, _, _]) =
    ()

  override def afterQuery(
      queryVal: QueryVal,
      context: MiddlewareQueryContext[GraphQlContext, _, _]
  ) = ()

  override def beforeField(
      queryVal: QueryVal,
      mctx: MiddlewareQueryContext[GraphQlContext, _, _],
      ctx: Context[GraphQlContext, _]
  ) = {
    val secureContext  = ctx.ctx
    val permissionTags = ctx.field.tags.filter(_.isInstanceOf[Permission])

    secureContext.user match {
      case Some(usr) =>
        val hasPermission = permissionTags.foldLeft(List.empty[Boolean]) {
          case (restrict, tag) =>
            tag match {

              case IsSelf =>
                if (!ctx.argDefinedInQuery(SchemaDefinition.UserIdArgument)) {
                  true :: restrict
                } else {
                  val res = ctx.arg(SchemaDefinition.UserIdArgument).contains(usr.userId)
                  res :: restrict
                }

              case WithRole(roles) =>
                roles.exists(usr.roles.contains) :: restrict

              case _ =>
                restrict
            }
        }
        if (hasPermission.isEmpty) {
          continue
        } else if (hasPermission.contains(true)) {
          continue
        } else {
          throw SecurityException("Operation not permitted")
        }

      case None =>
        throw SecurityException("Operation requires a logged in user")
    }
  }
}

object Permissions {

  sealed trait Permission extends FieldTag

  object IsSelf                         extends Permission
  case class WithRole(roles: Seq[Role]) extends Permission

}

case class SecurityException(message: String) extends Exception(message)
