package serenity.users

import java.time.LocalDate

import akka.actor.Props
import akka.actor.Status.{Failure, Success}
import akka.persistence.PersistentActor
import serenity.cqrs.{EventMeta, Evt}
import serenity.eventbrite._
import serenity.protobuf.userevents.MembershipUpdateMessage.EventbriteInformation
import serenity.users.UserReadProtocol._
import serenity.users.UserWriteProtocol.{HospesAuthSource, _}
import serenity.users.domain._

class UserActor(id: UserId) extends PersistentActor {

  private var user: Option[User] = None
  private var credentials: Option[BasicAuth] = None

  override def persistenceId: String = s"user-${id.toString}"

  override def receiveRecover: Receive = {
    case msg: UserUpdatedEvt => updateUserModel(msg)
    case msg: HospesUserImportEvt => updateUserModel(msg)
    case msg: MembershipUpdateEvt => updateUserModel(msg)
  }

  override def receiveCommand: Receive = {
    case cmd: HospesImportCmd if user.isDefined =>
      sender() ! Failure(ValidationFailed("User exist"))
    case HospesImportCmd(hospesUser) =>
      persistAll(List(
        toHospesUserEvent(id, hospesUser),
        BasicAuthEvt(id, hospesUser.password_pw, hospesUser.password_slt, HospesAuthSource)
      ) ++ hospesUser.memberships
          .map(m => MembershipUpdateEvt(
            LocalDate.of(m.year, 1, 1),
            MembershipAction.Add,
            MembershipIssuer.JavaBin,
            None))) {
        case evt: HospesUserImportEvt =>
          updateUserModel(evt)
          sender() ! Success("User created")
        case evt: BasicAuthEvt =>
          credentials = Some(HospesAuth(evt.password, evt.salt))
        case evt: MembershipUpdateEvt =>
          updateUserModel(evt)
      }
    case cmd@CreateOrUpdateUserCmd(attendee) =>
      val p = attendee.profile
      val m = attendee.attendeeMeta

      val userEvt = UserUpdatedEvt(id, p.email, p.firstName, p.lastName, p.phone, EventMeta())
      val events = toMembershipEvent(attendee, m) match {
        case Some(membershipEvt) => userEvt :: membershipEvt :: Nil
        case None => userEvt :: Nil
      }
      persistAll(events) {
        case evt: UserUpdatedEvt =>
          updateUserModel(evt)
          sender() ! Success("")
        case evt: MembershipUpdateEvt =>
          updateUserModel(evt)
      }
    case GetUser(queriedId) =>
      if (id != queriedId) sender() ! UserNotFound
      else sender() ! user.map(UserResponse).getOrElse(UserNotFound)
    case GetUserCredentials(cred) =>
      if (hasEmail(cred)) sender() ! credentials.map(UserCredentialsResponse).getOrElse(CredentialsNotFound)
      else sender() ! CredentialsNotFound
    case m@_ => sender() ! Failure(new IllegalArgumentException("Unhandled message"))
  }

  private def toMembershipEvent(attendee: Attendee, m: AttendeeMeta): Option[MembershipUpdateEvt] = {
    val hasMatchingMembershipEvent = user.exists(_.memberships.exists(
      _.eventbriteInformation.exists { case EventbriteInformation(aId, eId, oId) =>
        aId == m.id && eId == m.eventId && oId == m.orderId
      }))

    def createEvent(action: MembershipAction.Action) = {
      Some(MembershipUpdateEvt(
        attendee.attendeeMeta.created.toLocalDate,
        action,
        attendee.store match {
          case EventbriteStore.javaBin => MembershipIssuer.JavaBin
          case EventbriteStore.javaZone => MembershipIssuer.JavaZone
        },
        Some(EventbirteMeta(m.id, m.eventId, m.orderId))
      ))
    }

    attendee.attendeeMeta.status match {
      case Update if !hasMatchingMembershipEvent =>
        createEvent(MembershipAction.Add)
      case Delete if hasMatchingMembershipEvent =>
        createEvent(MembershipAction.Remove)
      case _ =>
        None
    }
  }

  private def hasEmail(email: String): Boolean =
    user.exists(_.emails.exists(_.address == email)) ||
        user.exists(_.mainEmail.address == email)

  private def updateUserModel(evt: Evt) =
    evt match {
      case evt: HospesUserImportEvt => user = EventToUser(evt)
      case evt: UserUpdatedEvt => user = EventToUser(evt, user)
      case evt: MembershipUpdateEvt => user = user.map(u => {
        val evtMembership = Membership(
          evt.from,
          evt.from.plusYears(1).minusDays(1),
          evt.issuer,
          evt.eventbirteMeta.map(em =>
            EventbriteInformation(em.attendeeId, em.eventId, em.orderId)))
        u.copy(
          memberships =
              evt.action match {
                case MembershipAction.Add =>
                  u.memberships + evtMembership
                case MembershipAction.Remove =>
                  u.memberships.filter(m => m.eventbriteInformation != evtMembership.eventbriteInformation)
              }
        )
      })
      case _ =>
    }
}

object UserActor {

  def apply(id: UserId): Props = Props.create(classOf[UserActor], id)

}

object EventToUser {

  def apply(evt: HospesUserImportEvt): Option[User] = Some(User(
    evt.id,
    evt.email.head,
    evt.email.tail,
    evt.phoneNumber,
    evt.meta.created,
    evt.firstName,
    evt.lastName,
    evt.address))

  def apply(evt: UserUpdatedEvt, user: Option[User]): Option[User] = {
    user.map(_.copy(
      firstName = Some(evt.firstName),
      lastName = Some(evt.lastName)
    )).orElse(Some(
      User(
        uuid = evt.id,
        mainEmail = Email(evt.email, validated = true),
        createdDate = evt.meta.created,
        firstName = Some(evt.firstName),
        lastName = Some(evt.lastName)
      )))

  }
}