package setup

import javax.inject.Inject

import controllers.helpers.RouterCtrl
import play.api.routing.{Router, SimpleRouter}

class AppRoutes @Inject()(controllers: Set[RouterCtrl]) extends SimpleRouter {

  override val routes: Router.Routes =
    controllers.map(_.withRoutes()).reduce(_ orElse _)

}
