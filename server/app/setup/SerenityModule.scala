package setup

import com.google.inject.AbstractModule
import controllers._
import controllers.helpers.RouterCtrl
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.codingwell.scalaguice.{ScalaModule, ScalaMultibinder}
import play.api.{Configuration, Environment}
import play.api.libs.concurrent.AkkaGuiceSupport
import services.eventbrite.EventbriteTokens
import repositories.eventsource.users.UserManagerActor

class SerenityModule( environment: Environment, config: Configuration) extends AbstractModule with AkkaGuiceSupport with ScalaModule {

  override def configure(): Unit = {
    val ctrls = ScalaMultibinder.newSetBinder[RouterCtrl](binder)
    ctrls.addBinding.to[EventbriteWebHooksCtrl].asEagerSingleton()
    ctrls.addBinding.to[HospesImportCtrl].asEagerSingleton()
    ctrls.addBinding.to[LoginCtrl].asEagerSingleton()
    ctrls.addBinding.to[PingCtrl].asEagerSingleton()

    bind[EventbriteWebHookConfig]
        .toInstance(EventbriteWebHookConfig(config.underlying.getString("serenity.eventbrite.secret")))
    bind[EventbriteTokens].toInstance(config.underlying.as[EventbriteTokens]("serenity.eventbrite.token"))
    bindActor[UserManagerActor]("UserManagerActor", (p) => UserManagerActor.apply())
  }

}
