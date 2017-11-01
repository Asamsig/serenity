package controllers

import javax.inject.{Inject, Singleton}

import auth.{DefaultEnv, WithRole}
import com.mohiva.play.silhouette.api.Silhouette
import controllers.helpers.RouterCtrl
import models.user.Roles.AdminRole
import models.user.UserId
import play.api.mvc.{Action, Results}
import play.api.routing.Router.Routes
import play.api.routing.sird._
import services.UpdateUserViewService

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

@Singleton
class AdminCtrl @Inject()(
    silhouette: Silhouette[DefaultEnv],
    updateUserViewService: UpdateUserViewService
)(implicit ec: ExecutionContext)
  extends RouterCtrl {
  override def withRoutes(): Routes = {
    case PUT(p"/api/admin/updaeview/all") => updateViewAll()
    case PUT(p"/api/admin/updaeview/$id") => updateView(id)
  }

  import silhouette.SecuredAction

  def updateViewAll(): Action[Unit] =
    SecuredAction(WithRole(AdminRole)).async(parse.empty) { _ =>
      updateUserViewService.updateAll().map(_ => Results.Ok).recover {
        case NonFatal(t) => Results.InternalServerError(t.getMessage)
      }
    }

  def updateView(id: String): Action[Unit] =
    SecuredAction(WithRole(AdminRole)).async(parse.empty) { _ =>
      UserId
        .fromString(id)
        .map(uuid => {
          updateUserViewService.updateUser(uuid).map(_ => Results.Ok).recover {
            case NonFatal(t) => Results.InternalServerError(t.getMessage)
          }
        })
        .getOrElse(Future.successful(Results.BadRequest(s"Invalid input $id")))
    }

}
