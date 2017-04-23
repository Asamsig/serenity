package controllers

import java.util.UUID
import javax.inject.{Inject, Singleton}

import auth.{DefaultEnv, WithRole}
import com.mohiva.play.silhouette.api.Silhouette
import controllers.helpers.RouterCtrl
import play.api.mvc.BodyParsers.parse
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.Results
import play.api.routing.sird._
import repositories.eventsource.users.domain.AdminRole
import services.UpdateUserViewService

import scala.concurrent.Future
import scala.util.Try
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
      Try {UUID.fromString(id)}
          .map(uuid => {
            updateUserViewService.updateUser(uuid)
                .map(_ => Results.Ok)
                .recover {
                  case NonFatal(t) => Results.InternalServerError(t.getMessage)
                }
          })
          .recover {
            case e: IllegalArgumentException =>
              Future.successful(Results.BadRequest(s"Invalid input ${e.getMessage}"))
          }
          .getOrElse(Future.successful(Results.InternalServerError))
  }

}
