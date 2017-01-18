package serenity.users

import java.util.UUID

import akka.actor.{ActorRef, Props}
import akka.persistence.PersistentActor
import akka.persistence.query.EventEnvelope
import akka.testkit.TestProbe
import serenity.UtcDateTime.nowUTC
import serenity.akka.{AkkaConfig, AkkaSuite, InMemoryCleanup}
import serenity.users.UserManagerActorFixtures.beerDuke
import serenity.users.UserProtocol.read.{CredentialsNotFound, GetUser, GetUserCredentials, GetUserWithEmail}
import serenity.users.UserProtocol.write._
import serenity.users.domain.{Email, UserId}

import scala.util.Failure

class UserManagerActorSpec extends AkkaSuite("UserManagerActorSpec", AkkaConfig.inMemoryPersistence())
    with InMemoryCleanup {

  def defaultSetup() = new {
    val probe: TestProbe = new TestProbe(system)
    val props: (UserId) => Props = (id: UserId) => {
      val ref: ActorRef = probe.ref
      Props(classOf[UsrActor], id, ref)
    }
    val actor: ActorRef = system.actorOf(UserManagerActor(props))
  }

  describe("Command messages") {
    describe("HospesImportCmd") {
      it("should forward msg") {
        val setup = defaultSetup()
        val cmd = HospesImportCmd(beerDuke)

        setup.actor ! cmd

        setup.probe.expectMsg(cmd)
      }

      it("should forward msg with unique email") {
        val setup = defaultSetup()
        val cmd = HospesImportCmd(beerDuke)
        val cmd2 = HospesImportCmd(beerDuke.copy(email = List(Email("not_beerduke@java.no", validated = true))))

        setup.actor ! cmd
        setup.actor ! cmd2

        setup.probe.expectMsgAllOf(cmd, cmd2)
      }

      it("should reject 2nd msg with same email") {
        val setup = defaultSetup()
        val cmd = HospesImportCmd(beerDuke)

        setup.actor ! cmd
        setup.actor ! cmd

        expectMsgClass(classOf[Failure[ValidationFailed]])
      }
    }

    describe("CreateUserCmd") {
      val cmd = CreateUserCmd("tada@java.no", "ta", "da")

      it("should forward msg") {
        val setup = defaultSetup()

        setup.actor ! cmd

        setup.probe.expectMsg(cmd)
      }

      it("should forward msg with unique email") {
        val setup = defaultSetup()
        val cmd2: CreateUserCmd = cmd.copy(email = "heh@java.no")

        setup.actor ! cmd
        setup.actor ! cmd2

        setup.probe.expectMsgAllOf(cmd, cmd2)
      }

      it("should reject 2nd with same email ") {
        val setup = defaultSetup()

        setup.actor ! cmd
        setup.actor ! cmd

        expectMsgClass(classOf[Failure[ValidationFailed]])
      }
    }
  }

  describe("Query messages") {
    def withEnvelope(cmd: HospesImportCmd): EventEnvelope = {
      EventEnvelope(
        1, "", 2,
        UserProtocol.write.toHospesUserEvent(UUID.randomUUID(), cmd.user))
    }

    describe("GetUserWithEmail") {
      it("should respond with failure if user doesn't exists") {
        val setup = defaultSetup()

        setup.actor ! GetUserWithEmail("doesnt.exist@java.no")

        expectMsgClass(classOf[Failure[String]])
      }

      it("should forward message if user exists") {
        val setup = defaultSetup()
        val query: GetUserWithEmail = GetUserWithEmail(beerDuke.email.head.address)

        val cmd: HospesImportCmd = HospesImportCmd(beerDuke)
        setup.actor ! cmd
        setup.probe.expectMsg(cmd)

        setup.actor ! query
        setup.probe.expectMsgClass(classOf[GetUser])
      }

    }

    describe("GetUserCredentials") {
      it("should respond with CredentialsNotFound if user doesn't exists") {
        val setup = defaultSetup()

        setup.actor ! GetUserCredentials("doesnt.exist@java.no")

        expectMsg(CredentialsNotFound)
      }

      it("should forward message if user exists ") {
        val setup = defaultSetup()
        val query: GetUserCredentials = GetUserCredentials(beerDuke.email.head.address)

        val cmd: HospesImportCmd = HospesImportCmd(beerDuke)
        setup.actor ! cmd
        setup.probe.expectMsg(cmd)

        setup.actor ! query
        setup.probe.expectMsgClass(classOf[GetUserCredentials])
      }
    }
  }
}

class UsrActor(id: UserId, probeRef: ActorRef) extends PersistentActor {
  override def persistenceId: String = "UserManagerActorSpec"

  override def receiveRecover: Receive = {
    case m => unhandled(m)
  }

  override def receiveCommand: Receive = {
    case m: HospesImportCmd => persist(toHospesUserEvent(id, m.user)) {
      evt =>
        probeRef.forward(m)
    }
    case m: CreateUserCmd => persist(UserRegisteredEvt(id, m.email, m.firstName, m.lastName, nowUTC())) {
      evt => probeRef.forward(m)
    }
    case m => probeRef.forward(m)
  }
}

object UserManagerActorFixtures {
  val beerDuke: HospesUser = {
    HospesUser(
      originId = List(42),
      email = List(Email("beerduke@java.no", validated = true)),
      firstname = Some("beer"),
      lastname = Some("duke"),
      address = None,
      phonenumber = None,
      password_pw = "gimme",
      password_slt = "beeeer!",
      memberships = Set(HospesMembership(49, 2016))
    )
  }

}