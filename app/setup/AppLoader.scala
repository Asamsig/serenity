package setup

import javax.inject.{Inject, Provider, Singleton}

import play.api.http.HttpConfiguration
import play.api.inject.guice.{GuiceApplicationLoader, GuiceableModule}
import play.api.routing.Router
import play.api.{ApplicationLoader, inject}

class AppLoader extends GuiceApplicationLoader {
  protected override def overrides(context: ApplicationLoader.Context): Seq[GuiceableModule] = {
    super.overrides(context) :+ (inject.bind[Router].toProvider[ScalaRoutesProvider]: GuiceableModule)
  }
}

@Singleton
class ScalaRoutesProvider @Inject()(
    router: AppRoutes,
    httpConfig: HttpConfiguration) extends Provider[Router] {

  override lazy val get = router.withPrefix(httpConfig.context)

}
