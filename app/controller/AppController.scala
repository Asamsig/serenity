package controller

import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import play.api.mvc.{Action, Results}

@Singleton
class AppController @Inject()(system: ActorSystem) {

  def index() = Action {
    Results.Ok("Hello world")
  }

}
