package repositories.view

import javax.inject.{Inject, Singleton}

import models.{UserId, time}
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import repositories.eventsource.users.domain
import repositories.eventsource.users.domain._

import scala.concurrent.Future

@Singleton
class SqlUserRepository @Inject()(val dbConfigProvider: DatabaseConfigProvider) extends Tables with UserRepository {

  import profile.api._

  def toEmail(email: UserEmailRow) = Email(email._2, email._4)

  def toMembership(m: UserMembershipRow): Membership = {
    Membership(
      from = m._2,
      to = m._3,
      issuer = m._4,
      List(m._5, m._6, m._7).flatten match {
        case a :: e :: o :: Nil => Some(EventbriteMeta(a, e, o))
        case _ => None
      })
  }

  def toUser(
      usr: Option[UserRow],
      email: Seq[UserEmailRow],
      roles: Seq[UserRoleRow],
      memberships: Seq[UserMembershipRow]
  ): Option[User] = {
    val primaryEmail = email.find(_._3)
    if (primaryEmail.isEmpty) None
    else usr.map(u => {
      val id: domain.UserId = u._1.underling
      User(
        uuid = id,
        firstName = u._2,
        lastName = u._3,
        mainEmail = toEmail(primaryEmail.get),
        emails = email.filter(!_._3).map(toEmail),
        phone = u._4,
        address = u._5,
        createdDate = u._6,
        roles = roles.map(_._2).toSet,
        memberships = memberships.map(toMembership).toSet
      )
    })
  }

  private def removeUserAction(uid: UserId) = for {
    _ <- usersTable.filter(_.userId === uid).delete
    _ <- userEmailsTable.filter(_.userId === uid).delete
    _ <- userRoleTable.filter(_.userId === uid).delete
    _ <- userMembershipsTable.filter(_.userId === uid).delete
  } yield ()

  private def insertUserAction(uid: UserId, u: User) = {
    val row: UserRow = (uid, u.firstName, u.lastName, u.phone, u.address, u.createdDate, time.dateTimeNow())
    usersTable += row
  }

  private def insertEmailsAction(uid: UserId, emails: List[Email]) =
    userEmailsTable ++= emails.map(e => {
      val tuple: UserEmailRow = (uid, e.address, e.address == emails.head.address, e.validated)
      tuple
    })

  private def insertRoleAction(uid: UserId, roles: Set[Role]) =
    userRoleTable ++= roles.map(r => {
      val row: UserRoleRow = (uid, r)
      row
    })

  private def insertMembershipAction(uid: UserId, memberships: Set[Membership]) =
    userMembershipsTable ++= memberships.map(m => {
      val row: UserMembershipRow = (
          uid,
          m.from,
          m.to,
          m.issuer,
          m.eventbriteMeta.map(_.attendeeId),
          m.eventbriteMeta.map(_.eventId),
          m.eventbriteMeta.map(_.orderId)
      )
      row
    })

  def saveUser(u: User): Future[Unit] = {
    val uid: UserId = UserId(u.uuid)
    val insertUserData = for {
      _ <- insertUserAction(uid, u)
      _ <- insertEmailsAction(uid, u.allEmail)
      _ <- insertRoleAction(uid, u.roles)
      _ <- insertMembershipAction(uid, u.memberships)
    } yield ()
    val res = db.run(DBIO.seq(removeUserAction(uid), insertUserData))
    res
  }

  def saveCredentials(id: UserId, auth: BasicAuth): Future[Unit] = {
    val value: UserCredentialsRow = auth match {
      case HospesAuth(pwd, salt) => (id, 1, pwd, salt)
      case SerenityAuth(pwd) => (id, 2, pwd, None)
    }
    val action = userCredentialsTable.insertOrUpdate(value)
    db.run(action).map(_ => ())
  }

  def fetchUserById(userId: UserId): Future[Option[User]] = {
    val query = for {
      usr <- usersTable.filter(_.userId === userId).result.headOption
      emails <- userEmailsTable.filter(_.userId === userId).result
      roles <- userRoleTable.filter(_.userId === userId).result
      memberships <- userMembershipsTable.filter(_.userId === userId).result
    } yield toUser(usr, emails, roles, memberships)

    db.run(query)
  }

  def findUserIdByEmail(email: String): Future[Option[UserId]] = {
    val query = userEmailsTable.filter(_.email === email).map(_.userId)
    db.run(query.result.headOption)
  }

  def credentialsByEmail(email: String): Future[Option[BasicAuth]] = {
    val query = for {
      (_, cred) <- userEmailsTable.filter(_.email === email).join(userCredentialsTable).on(_.userId === _.userId)
    } yield cred

    db.run(query.result.headOption).map(
      _.map { case (_, typ, pwd, salt) =>
        typ match {
          case 1 => HospesAuth(pwd, salt)
          case 2 => SerenityAuth(pwd)
        }
      }
    )
  }
}
