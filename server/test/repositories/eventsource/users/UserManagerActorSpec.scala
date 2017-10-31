package repositories.eventsource.users

import akka.actor.{Actor, ActorRef, Props}
import akka.testkit.TestProbe
import helpers.akka.AkkaSuite
import models._
import models.user.Auths.{BasicAuth, SerenityAuth}
import models.user.{Auths, Email, User, UserId}
import org.scalamock.scalatest.MockFactory
import repositories.eventsource.users.UserReadProtocol._
import repositories.eventsource.users.UserWriteProtocol._
import repositories.view.UserRepository

import scala.concurrent.Future
import scala.concurrent.duration.DurationDouble
import scala.util.Failure

class UserManagerActorSpec extends AkkaSuite("UserManagerActorSpec") with MockFactory {
  private val user = User(
    UserId.generate(),
    Email("beerduke@java.no", true),
    Seq(),
    createdDate = time.dateTimeNow()
  )
  private val emailExist   = user.mainEmail
  private val emailMissing = Email("does-not-exist@java.no", false)

  private val cred = SerenityAuth("pwd")
  private val repo = new TestUserRepository(Set(user), Map(user.userId -> cred))

  class DefaultTestSetup {
    val probe: TestProbe = new TestProbe(system)
    val props: (UserRepository, UserId) => Props = (_: UserRepository, id: UserId) => {
      val ref: ActorRef = probe.ref
      Props(classOf[ForwardActor], id, ref)
    }
    val userManager: ActorRef = system.actorOf(UserManagerActor(repo, props))
  }

  def defaultSetup() = new DefaultTestSetup

  describe("UserManager") {

    describe("receive CreateOrUpdateUserCmd") {

      it("forward it when user doesn't exist") {
        val attendee = Attendee(
          Profile("", "", "", "example@java.no"),
          mock[AttendeeMeta],
          EventbriteStore.javaBin
        )
        val cmd   = CreateOrUpdateUserCmd(attendee)
        val setup = defaultSetup()
        setup.userManager ! cmd
        setup.probe.expectMsg(cmd)
      }

      it("forward it when user does exist") {
        val attendee = Attendee(
          Profile("", "", "", user.mainEmail.address),
          mock[AttendeeMeta],
          EventbriteStore.javaBin
        )
        val cmd   = CreateOrUpdateUserCmd(attendee)
        val setup = defaultSetup()
        setup.userManager ! cmd
        setup.probe.expectMsg(cmd)
      }
    }

    describe("receive UpdateUserProfileCmd") {

      it("do not forward it when user doesn't exist") {
        val cmd   = UpdateUserProfileCmd(UserId.generate(), "fn", "ln", "adr", "phone")
        val setup = defaultSetup()
        setup.userManager ! cmd
        expectMsg(Failure(ValidationFailed("User doesn't exist")))
      }

      it("forward it when user does exist") {
        val cmd   = UpdateUserProfileCmd(user.userId, "fn", "ln", "adr", "phone")
        val setup = defaultSetup()
        setup.userManager ! cmd
        setup.probe.expectMsg(cmd)
      }
    }

    describe("receive HospesImportCmd") {
      it("forward it when user doesn't exist") {
        val cmd = HospesImportCmd(
          HospesUser(
            List(),
            List(emailMissing),
            None,
            None,
            None,
            None,
            "pwd",
            "salt",
            Set()
          )
        )
        val setup = defaultSetup()
        setup.userManager ! cmd
        setup.probe.expectMsg(cmd)
      }

      it("decline it when user does exist") {
        val cmd = HospesImportCmd(
          HospesUser(
            List(),
            List(emailExist),
            None,
            None,
            None,
            None,
            "pwd",
            "salt",
            Set()
          )
        )
        val setup = defaultSetup()
        setup.userManager ! cmd

        setup.probe.expectNoMsg(100 millis)
        expectMsg(Failure(ValidationFailed("User exist")))
      }
    }

    describe("receive UpdateCredentialsCmd") {
      it("forward it when user does exist") {
        val cmd   = UpdateCredentialsCmd(emailExist.address, "hash")
        val setup = defaultSetup()
        setup.userManager ! cmd
        setup.probe.expectMsg(cmd)
      }

      it("decline it when user doesn't exist") {
        val cmd   = UpdateCredentialsCmd(emailMissing.address, "hash")
        val setup = defaultSetup()
        setup.userManager ! cmd

        setup.probe.expectNoMsg(100 millis)
        expectMsg(Failure(ValidationFailed("User does not exist")))
      }
    }

    describe("receive GetUserWithEmail query") {
      it("forward it when user does exist") {
        val query = GetUserWithEmail(emailExist.address)
        val setup = defaultSetup()
        setup.userManager ! query

        setup.probe.expectMsg(GetUser(user.userId))
      }

      it("decline it when user doesn't exist") {
        val query = GetUserWithEmail(emailMissing.address)
        val setup = defaultSetup()
        setup.userManager ! query

        setup.probe.expectNoMsg(100 millis)
        expectMsg(Failure(ValidationFailed("User with email doesn't exist")))
      }
    }

    describe("receive GetUserCredentials query") {
      it("forward it when user does exist") {
        val query = GetUserCredentials(emailExist.address)
        val setup = defaultSetup()
        setup.userManager ! query

        setup.probe.expectMsg(query)
      }

      it("decline it when user doesn't exist") {
        val query = GetUserCredentials(emailMissing.address)
        val setup = defaultSetup()
        setup.userManager ! query

        setup.probe.expectNoMsg(100 millis)
        expectMsg(CredentialsNotFound)
      }
    }

    describe("receive UpdateView query") {
      it("forward it") {
        val query = UpdateView(user.userId)
        val setup = defaultSetup()
        setup.userManager ! query

        setup.probe.expectMsg(query)
      }
    }
  }
}

class ForwardActor(id: UserId, probeRef: ActorRef) extends Actor {

  override def receive: Receive = {
    case m => probeRef.forward(m)
  }
}

class TestUserRepository(
    var users: Set[User] = Set.empty,
    var credentials: Map[UserId, BasicAuth] = Map.empty
) extends UserRepository {

  override def saveUser(u: User) = {
    users = users + u
    Future.successful(())
  }

  override def saveCredentials(id: UserId, auth: Auths.BasicAuth) = {
    credentials = credentials + (id -> auth)
    Future.successful(())
  }

  override def countUsers() =
    Future.successful(users.size)

  override def fetchUserById(userId: UserId) =
    Future.successful(users.find(_.userId == userId))

  override def findUserIdByEmail(email: String) =
    Future.successful(users.find(_.allEmail.exists(_.address == email)).map(_.userId))

  override def findUsersIdByEmail(email: Seq[String]) =
    Future.successful(
      users.filter(_.allEmail.exists(e => email.contains(e.address))).map(_.userId).toSeq
    )

  override def credentialsByEmail(email: String) =
    Future.successful(
      users
        .find(_.allEmail.exists(_.address == email))
        .map(_.userId)
        .flatMap(u => credentials.get(u))
    )

}
