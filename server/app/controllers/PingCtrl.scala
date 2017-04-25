package controllers

import javax.inject.{Inject, Singleton}

import controllers.helpers.RouterCtrl
import play.api.mvc.{Action, Results}
import play.api.routing.Router.Routes
import play.api.routing.sird._

@Singleton
class PingCtrl @Inject()() extends RouterCtrl {

  override def withRoutes(): Routes = {
    case GET(p"/api/ping") => ping()
  }

  def ping() = Action {
    Results.Ok("pong")
  }

}
