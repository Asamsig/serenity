package controller

import javax.inject.{Inject, Singleton}

import play.api.mvc.{Action, Results}

@Singleton
class PingCtrl @Inject()() {

  def ping() = Action {
    Results.Ok("pong")
  }

}
