package setup

import javax.inject.Inject

import controller.{HospesImportCtrl, LoginCtrl, PingCtrl}
import play.api.routing.Router.Routes
import play.api.routing.sird._
import play.api.routing.{Router, SimpleRouter}

class AppRoutes @Inject()(
    pingCtrl: PingCtrl,
    loginCtrl: LoginCtrl,
    hospesImportCtrl: HospesImportCtrl
) extends SimpleRouter {

  override def routes: Routes = {
    Router.from {
      case GET(p"/ping") => pingCtrl.ping()
      case POST(p"/hospes/import") => hospesImportCtrl.importData()

      case POST(p"/login") => loginCtrl.login()
      case GET(p"/logout") => loginCtrl.logout()
    }.routes
  }

}
