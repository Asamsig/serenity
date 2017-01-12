package setup

import javax.inject.Inject

import akka.actor.{ActorRef, ActorSystem}
import com.google.inject.AbstractModule
import com.google.inject.name.Names
import play.api.libs.concurrent.AkkaGuiceSupport
import serenity.users.UserManagerActor

class SerenityActorsDI @Inject()(actorSystem: ActorSystem) extends AbstractModule with AkkaGuiceSupport {

  import SerenityActorsDI._

  override def configure(): Unit = {
    actorFromRef(
      userManagerActor,
      actorSystem.actorOf(UserManagerActor.apply()))
  }

  def actorFromRef(name: String, actorRef: ActorRef): Unit =
    bind(classOf[ActorRef])
        .annotatedWith(Names.named(name))
        .toInstance(actorRef)
}


object SerenityActorsDI {
  val userManagerActor = classOf[UserManagerActor].getSimpleName
}
