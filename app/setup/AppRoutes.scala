package setup

import javax.inject.Inject

import controller.AppController
import play.api.routing.Router.Routes
import play.api.routing.sird._
import play.api.routing.{Router, SimpleRouter}

class AppRoutes @Inject()(
    appController: AppController
) extends SimpleRouter {

  override def routes: Routes = {
    Router.from {
      case GET(p"/") => appController.index()
    }.routes
  }

}
