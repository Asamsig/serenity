package services

import javax.inject.{Inject, Named, Singleton}

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.persistence.query.scaladsl.{CurrentEventsByTagQuery2, EventsByTagQuery2}
import akka.persistence.query.{Offset, PersistenceQuery}
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Keep, Sink}
import akka.util.Timeout
import models.user.UserId
import play.api.Logger
import repositories.eventsource.users.UserReadProtocol.UpdateView
import repositories.eventsource.users.UserWriteProtocol.{HospesUserImportEvt, UserUpdatedEvt}
import repositories.eventsource.{DomainReadEventAdapter, Tags}

import scala.concurrent.duration.DurationDouble
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class UpdateUserViewService @Inject()(
    as: ActorSystem,
    @Named("UserManagerActor") userManagerActor: ActorRef
)(implicit mat: Materializer, ec: ExecutionContext) {

  val logger = Logger(classOf[UpdateUserViewService])

  implicit val timeout: Timeout = 360.seconds

  def updateAll(): Future[Any] = {
    val toMsg  = new DomainReadEventAdapter
    val source = journal.currentEventsByTag(Tags.USER_EMAIL, Offset.noOffset)

    val sink = Flow[UserId]
      .mapAsync(1)(uid => this.updateUser(uid))
      .toMat(Sink.ignore)(Keep.right)
      .named("upd-usr-view")

    val groupedUserIdsStream = source.map { env =>
      toMsg.fromMessage(env.event) match {
        case u: HospesUserImportEvt => Some(u.id)
        case u: UserUpdatedEvt      => Some(u.id)
        case m                      => None
      }
    }.grouped(500)

    groupedUserIdsStream.mapConcat(_.flatten.distinct).runWith(sink)
  }

  private def journal: CurrentEventsByTagQuery2 with EventsByTagQuery2 = {
    val journalPluginId =
      as.settings.config.getString("serenity.persistence.query-journal")
    PersistenceQuery(as).readJournalFor(journalPluginId)
  }

  def updateUser(userId: UserId): Future[UpdateView] = {
    val res = (userManagerActor ? UpdateView(userId)).flatMap {
      case uv: UpdateView =>
        Future.successful(uv)

      case Success(v) if v.isInstanceOf[UpdateView] =>
        Future.successful(v.asInstanceOf[UpdateView])

      case Failure(t) =>
        logger.warn(s"Failed to update view $userId", t)
        Future.failed(t)

      case m =>
        logger.warn(s"Unknown message $userId")
        Future.failed(new IllegalStateException(s"unknown message: $m"))
    }
    res
  }

}
