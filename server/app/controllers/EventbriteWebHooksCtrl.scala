package controllers

import javax.inject.{Inject, Singleton}

import auth.DefaultEnv
import com.mohiva.play.silhouette.api.Silhouette
import controllers.helpers.{EnumPathExtractor, RouterCtrl}
import models.EventbriteHandleStatus.{Failure, NotSupported, Success}
import models.{EventbriteStore, EventbriteWebHook, WebHookDetails, WebHookInfoJson}
import play.api.mvc.Results
import play.api.mvc.Results.EmptyContent
import play.api.routing.Router.Routes
import play.api.routing.sird._
import services.eventbrite._

import scala.concurrent.{ExecutionContext, Future}

case class EventbriteWebHookConfig(secret: String)
@Singleton
class EventbriteWebHooksCtrl @Inject()(
    silhouette: Silhouette[DefaultEnv],
    config: EventbriteWebHookConfig,
    eventbriteService: EventbriteService
)(implicit ec: ExecutionContext)
  extends RouterCtrl
    with WebHookInfoJson {

  import silhouette.UnsecuredAction

  private val storePath = EnumPathExtractor.binders(EventbriteStore)

  override def withRoutes(): Routes = {
    case POST(p"/api/webhook/${storePath(store)}" ? q"token=$secret") =>
      this.webHook(store, secret)
  }

  def webHook(store: EventbriteStore.Store, secret: String) =
    UnsecuredAction.async(parse.json) { request =>
      if (config.secret != secret) {
        Future.successful(Results.Unauthorized("Unauthorized"))
      } else {
        val info = request.body.as[WebHookDetails]
        eventbriteService.handleWebHook(EventbriteWebHook(store, info)).map {
          case Success      => Results.Accepted(EmptyContent())
          case Failure      => Results.InternalServerError(EmptyContent())
          case NotSupported => Results.Gone(EmptyContent())
        }
      }
    }

}
