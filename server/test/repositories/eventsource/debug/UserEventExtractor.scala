package repositories.eventsource.debug

import java.util.UUID

import akka.actor.{Actor, ActorSystem}
import akka.persistence.query.PersistenceQuery
import akka.persistence.query.javadsl.EventsByPersistenceIdQuery
import akka.persistence.query.scaladsl.CurrentEventsByPersistenceIdQuery
import akka.stream.ActorMaterializer
import models.user.Auths.BasicAuth
import models.user.{User, UserId}
import repositories.eventsource.DomainReadEventAdapter
import repositories.eventsource.users.UserActor
import repositories.eventsource.users.UserReadProtocol.UpdateView
import repositories.view.UserRepository

import scala.concurrent.Future

object UserEventExtractor extends App {
  implicit val as = ActorSystem("UserExtractor")
  implicit val mat = ActorMaterializer()
  val toMsg = new DomainReadEventAdapter

  private def getJournal(): CurrentEventsByPersistenceIdQuery with EventsByPersistenceIdQuery = {
    val journalPluginId = as.settings.config.getString("serenity.persistence.query-journal")
    PersistenceQuery(as).readJournalFor(journalPluginId)
  }

  val source = getJournal().currentEventsByPersistenceId("user-7e46aab2-d5ca-499d-9908-f6716632a965", 0, Long.MaxValue)

  source.map(env => toMsg.fromMessage(env.event))
      .runForeach(e => println(e))

}

object StartUserActor extends App {
  implicit val as = ActorSystem("UserExtractor")
  implicit val mat = ActorMaterializer()
  val repo = new NoOpRepo

  private val id = UserId(UUID.fromString("7e46aab2-d5ca-499d-9908-f6716632a965"))
  val ua = as.actorOf(UserActor(repo, id))
  ua.tell(UpdateView(id), Actor.noSender)
}

class NoOpRepo extends UserRepository {
  override def saveUser(u: User) =
    Future.successful(())

  override def saveCredentials(id: UserId, auth: BasicAuth) =
    Future.successful(())

  override def fetchUserById(userId: UserId) = ???

  override def findUserIdByEmail(email: String) = ???

  override def credentialsByEmail(email: String) = ???

  override def countUsers() = ???
}