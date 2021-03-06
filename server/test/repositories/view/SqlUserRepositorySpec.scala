package repositories.view

import java.util.UUID

import models.time
import models.user.Auths.HospesAuth
import models.user.Memberships.{EventbriteMeta, Membership, MembershipIssuer}
import models.user.Roles.AdminRole
import models.user.{Email, User, UserId}
import org.scalatest.Inside
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

class SqlUserRepositorySpec
    extends PlaySpec
    with GuiceOneAppPerSuite
    with ScalaFutures
    with Inside {

  val repo = app.injector.instanceOf(classOf[SqlUserRepository])

  def uniqueEmail = Email(s"${UUID.randomUUID()}@java.no", validated = true)

  "user repository" should {
    "insert basic user without fault" in {
      val usr = User(
        userId = UserId.generate(),
        mainEmail = uniqueEmail,
        createdDate = time.dateTimeNow()
      )
      repo.saveUser(usr).futureValue
    }

    "insert and get user" in {
      val usr = User(
        userId = UserId.generate(),
        firstName = Some("first"),
        lastName = Some("last"),
        mainEmail = uniqueEmail,
        createdDate = time.dateTimeNow(),
        emails = Seq(uniqueEmail.copy(validated = false)),
        phone = Some("12300123"),
        address = Some("teknologihuset, 0000 oslo"),
        roles = Set(AdminRole),
        memberships = Set(
          Membership(
            from = time.dateTimeNow().minusDays(20).toLocalDate,
            to = time.dateTimeNow().plusDays(30).toLocalDate,
            issuer = MembershipIssuer.JavaBin,
            eventbriteMeta = None
          ),
          Membership(
            from = time.dateTimeNow().minusDays(20).toLocalDate,
            to = time.dateTimeNow().plusDays(30).toLocalDate,
            issuer = MembershipIssuer.JavaZone,
            eventbriteMeta = Some(EventbriteMeta("123", "234", "354"))
          )
        )
      )

      repo.saveUser(usr).futureValue
      val res = repo.fetchUserById(usr.userId).futureValue

      inside(res) { case Some(u) => matchUser(u, usr) }
    }

    "find credentials by email" in {
      val usr = User(
        userId = UserId.generate(),
        mainEmail = uniqueEmail,
        createdDate = time.dateTimeNow()
      )
      val auth = HospesAuth("pwd", Some("salt"))

      repo.saveUser(usr).futureValue
      repo.saveCredentials(usr.userId, auth).futureValue

      val res = repo.credentialsByEmail(usr.mainEmail.address).futureValue

      res mustBe Some(auth)
    }

    "find user id by email" in {
      val usr = User(
        userId = UserId.generate(),
        mainEmail = uniqueEmail,
        createdDate = time.dateTimeNow()
      )

      repo.saveUser(usr).futureValue
      val res = repo.findUserIdByEmail(usr.mainEmail.address).futureValue

      res mustBe Some(usr.userId)
    }

    def matchUser(actual: User, expected: User) = {
      actual.userId mustBe expected.userId
      actual.firstName mustBe expected.firstName
      actual.lastName mustBe expected.lastName
      actual.phone mustBe expected.phone
      actual.createdDate mustBe expected.createdDate
      actual.address mustBe expected.address
      actual.mainEmail mustBe expected.mainEmail
      actual.roles must contain theSameElementsAs expected.roles
      actual.memberships must contain theSameElementsAs expected.memberships
      actual.emails must contain theSameElementsAs expected.emails
    }
  }

}
