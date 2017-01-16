package setup

import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport
import serenity.users.UserManagerActor

class SerenityActorsDI extends AbstractModule with AkkaGuiceSupport {

  override def configure(): Unit = {
    bindActor[UserManagerActor]("UserManagerActor", (p) => UserManagerActor.apply())
  }

}
