package setup

import com.google.inject.AbstractModule
import controller.{EventbriteWebHooksCtrl, HospesImportCtrl, LoginCtrl, PingCtrl}
import controller.helpers.RouterCtrl
import net.codingwell.scalaguice.{ScalaModule, ScalaMultibinder}
import play.api.{Configuration, Environment}
import play.api.libs.concurrent.AkkaGuiceSupport
import serenity.users.UserManagerActor

class SerenityModule( environment: Environment, config: Configuration) extends AbstractModule with AkkaGuiceSupport with ScalaModule {

  override def configure(): Unit = {
    val ctrls = ScalaMultibinder.newSetBinder[RouterCtrl](binder)
    ctrls.addBinding.to[EventbriteWebHooksCtrl].asEagerSingleton()
    ctrls.addBinding.to[HospesImportCtrl].asEagerSingleton()
    ctrls.addBinding.to[LoginCtrl].asEagerSingleton()
    ctrls.addBinding.to[PingCtrl].asEagerSingleton()


    bind[String].annotatedWithName("eventbrite.secret").toInstance(config.getString("serenity.eventbrite.secret").get)
    bindActor[UserManagerActor]("UserManagerActor", (p) => UserManagerActor.apply())
  }

}
