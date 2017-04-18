package repositories.eventsource.users

import java.time.LocalDate

import akka.actor.Status.{Failure, Success}
import akka.actor.{ActorLogging, Props}
import akka.persistence.PersistentActor
import models._
import repositories.eventsource.users.UserReadProtocol._
import repositories.eventsource.users.UserWriteProtocol.{HospesAuthSource, _}
import repositories.eventsource.users.domain._

class UserActor(id: UserId) extends PersistentActor with ActorLogging {

  private var user: Option[User] = None
  private var credentials: Option[BasicAuth] = None

  override def persistenceId: String = s"user-${id.toString}"

  override def receiveRecover: Receive = {
    case msg: UserUpdatedEvt => updateUserModel(msg)
    case msg: HospesUserImportEvt => updateUserModel(msg)
    case msg: MembershipUpdateEvt => updateUserModel(msg)
    case msg: BasicAuthEvt => updateCredentialModel(msg)
  }

  override def receiveCommand: Receive = {

    case cmd: HospesImportCmd if user.isDefined =>
      sender() ! Failure(ValidationFailed("User exist"))

    case HospesImportCmd(hospesUser) =>
      persistAll(List(
        toHospesUserEvent(id, hospesUser),
        BasicAuthEvt(id, hospesUser.password_pw, Some(hospesUser.password_slt), HospesAuthSource)
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

    case UpdateCredentialsCmd(email, hashedPassword) =>
      if (user.isEmpty) sender() ! UserNotFound
      else user.foreach(u => {
        persist(BasicAuthEvt(u.uuid, hashedPassword)) { evt =>
          updateUserModel(evt)
          sender() ! UserCredentialsResponse(credentials.get)
        }
      })

    case GetUser(queriedId) =>
      if (id != queriedId) sender() ! UserNotFound
      else sender() ! user.map(UserResponse).getOrElse(UserNotFound)

    case GetUserCredentials(email) =>
      if (hasEmail(email)) {
        if (credentials.isEmpty) {
          log.info(s"No credentials for user ${user.map(_.uuid)} requesting $email")
        }
        sender() ! credentials.map(UserCredentialsResponse).getOrElse(CredentialsNotFound)
      } else {
        log.info(s"Email $email not recognized for user ${user.map(_.uuid)} ${user.map(_.allEmail)}")
        sender() ! CredentialsNotFound
      }

    case m@_ => sender() ! Failure(new IllegalArgumentException(s"Unhandled message of type ${m.getClass}"))
  }

  private def toMembershipEvent(attendee: Attendee, m: AttendeeMeta): Option[MembershipUpdateEvt] = {
    val hasMatchingMembershipEvent = user.exists(_.memberships.exists(
      _.eventbriteMeta.exists { case EventbriteMeta(aId, eId, oId) =>
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
        Some(EventbriteMeta(m.id, m.eventId, m.orderId))
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
    user.exists(_.allEmail.exists(_.address == email))

  private def updateCredentialModel(msg: BasicAuthEvt) = {
    msg.source match {
      case HospesAuthSource => credentials = Some(HospesAuth(msg.password, msg.salt))
      case SerenityAuthSource => credentials = Some(SerenityAuth(msg.password))
    }
  }

  private def updateUserModel(evt: Evt) =
    evt match {
      case evt: HospesUserImportEvt => user = EventToUser(evt)
      case evt: UserUpdatedEvt => user = EventToUser(evt, user)
      case evt: MembershipUpdateEvt => user = user.map(u => {
        val evtMembership = Membership(
          evt.from,
          evt.from.plusYears(1).minusDays(1),
          evt.issuer,
          evt.eventbirteMeta)
        u.copy(
          memberships =
              evt.action match {
                case MembershipAction.Add =>
                  u.memberships + evtMembership
                case MembershipAction.Remove =>
                  u.memberships.filter(m => m.eventbriteMeta != evtMembership.eventbriteMeta)
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