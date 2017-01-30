package controller

import javax.inject.{Inject, Named, Singleton}

import auth.DefaultEnv
import com.mohiva.play.silhouette.api.Silhouette
import controller.helpers.{EnumPathExtractor, RouterCtrl}
import play.api.mvc.Results.EmptyContent
import play.api.mvc.{Controller, Results}
import play.api.routing.Router.Routes
import play.api.routing.sird._
import serenity.eventbrite.EventbriteHandleStatus.{Failure, NotSupported, Success}
import serenity.eventbrite._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EventbriteWebHooksCtrl @Inject()(
    silhouette: Silhouette[DefaultEnv],
    @Named("eventbrite.secret") eventbriteSecret: String,
    eventbriteService: EventbriteService)
    (implicit ec: ExecutionContext)
    extends RouterCtrl with Controller with WebHookInfoJson {

  import silhouette.UnsecuredAction

  private val storePath = EnumPathExtractor.binders(EventbriteStore)

  override def withRoutes(): Routes = {
    case POST(p"/webhook/${storePath(store)}" ? q"token=$secret") => this.webHook(store, secret)
  }

  def webHook(store: EventbriteStore.Store, secret: String) = UnsecuredAction.async(parse.json) {
    request =>
      if (eventbriteSecret != secret)
        Future.successful(Results.Unauthorized("Unauthorized"))
      else {
        val info = request.body.as[WebHookDetails]
        eventbriteService.handleWebHook(EventbriteWebHook(store, info)).map {
          case Success => Results.Accepted(EmptyContent())
          case Failure => Results.InternalServerError(EmptyContent())
          case NotSupported => Results.Gone(EmptyContent())
        }
      }
  }

}
