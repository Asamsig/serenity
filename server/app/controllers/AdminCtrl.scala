package controllers

import javax.inject.{Inject, Singleton}

import auth.{DefaultEnv, WithRole}
import com.mohiva.play.silhouette.api.Silhouette
import controllers.helpers.RouterCtrl
import models.user.Roles.AdminRole
import models.user.UserId
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.BodyParsers.parse
import play.api.mvc.Results
import play.api.routing.sird._
import services.UpdateUserViewService

import scala.concurrent.Future
import scala.util.control.NonFatal

@Singleton
class AdminCtrl @Inject()(
    silhouette: Silhouette[DefaultEnv],
    updateUserViewService: UpdateUserViewService
) extends RouterCtrl {
  override def withRoutes() = {
    case PUT(p"/api/admin/updaeview/all") => updateViewAll()
    case PUT(p"/api/admin/updaeview/$id") => updateView(id)
  }

  import silhouette.SecuredAction

  def updateViewAll() = SecuredAction(WithRole(AdminRole)).async(parse.empty) { _ =>
    updateUserViewService.updateAll().map(_ => Results.Ok).recover {
      case NonFatal(t) => Results.InternalServerError(t.getMessage)
    }
  }

  def updateView(id: String) = SecuredAction(WithRole(AdminRole)).async(parse.empty) {
    _ =>
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
