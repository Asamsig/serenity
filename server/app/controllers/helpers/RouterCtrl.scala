package controllers.helpers

import play.api.mvc.InjectedController
import play.api.routing.Router.Routes

trait RouterCtrl extends InjectedController {

  def withRoutes(): Routes

}
