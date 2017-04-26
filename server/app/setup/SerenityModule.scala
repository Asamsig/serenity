package setup

import javax.inject.Inject

import akka.actor.{ActorRef, ActorSystem}
import com.google.inject.{AbstractModule, Provider}
import controllers._
import controllers.helpers.RouterCtrl
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.codingwell.scalaguice.{ScalaModule, ScalaMultibinder}
import play.api.libs.concurrent.AkkaGuiceSupport
import play.api.{Configuration, Environment}
import repositories.eventsource.users.UserManagerActor
import repositories.view.{SqlUserRepository, UserRepository}
import services.UpdateViewStarter
import services.eventbrite.EventbriteTokens

class SerenityModule(environment: Environment, config: Configuration)
    extends AbstractModule
    with AkkaGuiceSupport
    with ScalaModule {

  override def configure(): Unit = {
    val ctrls = ScalaMultibinder.newSetBinder[RouterCtrl](binder)
    ctrls.addBinding.to[EventbriteWebHooksCtrl].asEagerSingleton()
    ctrls.addBinding.to[HospesImportCtrl].asEagerSingleton()
    ctrls.addBinding.to[LoginCtrl].asEagerSingleton()
    ctrls.addBinding.to[PingCtrl].asEagerSingleton()
    ctrls.addBinding.to[AdminCtrl].asEagerSingleton()

    bind[UserRepository].to[SqlUserRepository]

    bind[EventbriteWebHookConfig].toInstance(
      EventbriteWebHookConfig(config.underlying.getString("serenity.eventbrite.secret"))
    )
    bind[EventbriteTokens].toInstance(
      config.underlying.as[EventbriteTokens]("serenity.eventbrite.token")
    )

    bind[ActorRef]
      .annotatedWithName("UserManagerActor")
      .toProvider(classOf[UserManagerActorProvider])
      .asEagerSingleton()

    bind[UpdateViewStarter].asEagerSingleton()
  }

}

class UserManagerActorProvider @Inject()(as: ActorSystem, repo: UserRepository)
    extends Provider[ActorRef] {
  override def get() = as.actorOf(UserManagerActor(repo), "user-manager")
}
