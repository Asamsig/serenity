package serenity.users

import akka.actor.{Actor, ActorRef, Props}
import akka.testkit.TestProbe
import serenity.akka.AkkaSuite
import serenity.users.UserManagerActorFixtures.beerDuke
import serenity.users.UserProtocol.read.{GetUser, GetUserWithEmail}
import serenity.users.UserProtocol.write._
import serenity.users.domain.{Email, UserId}

import scala.util.Failure

class UserManagerActorSpec extends AkkaSuite("UserManagerActorSpec") {

  def defaultSetup = new {
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
        val setup = defaultSetup
        val cmd = HospesImportCmd(beerDuke)

        setup.actor ! cmd

        setup.probe.expectMsg(cmd)
      }

      it("should forward msg with unique email") {
        val setup = defaultSetup
        val cmd = HospesImportCmd(beerDuke)
        val cmd2 = HospesImportCmd(beerDuke.copy(email = List(Email("not_beerduke@java.no", validated = true))))

        setup.actor ! cmd
        setup.actor ! cmd2

        setup.probe.expectMsg(cmd)
        setup.probe.expectMsg(cmd2)
      }

      it("should reject 2nd msg with same email") {
        val setup = defaultSetup
        val cmd = HospesImportCmd(beerDuke)

        setup.actor ! cmd
        setup.actor ! cmd

        expectMsgClass(classOf[Failure[ValidationFailed]])
      }
    }

    describe("CreateUserCmd") {
      val cmd = CreateUserCmd("tada@java.no", "ta", "da")

      it("should forward msg") {
        val setup = defaultSetup

        setup.actor ! cmd

        setup.probe.expectMsg(cmd)
      }

      it("should forward msg with unique email") {
        val setup = defaultSetup
        val cmd2: CreateUserCmd = cmd.copy(email = "heh@java.no")

        setup.actor ! cmd
        setup.actor ! cmd2

        setup.probe.expectMsg(cmd)
        setup.probe.expectMsg(cmd2)
      }

      it("should reject 2nd with same email ") {
        val setup = defaultSetup

        setup.actor ! cmd
        setup.actor ! cmd

        expectMsgClass(classOf[Failure[ValidationFailed]])
      }
    }
  }

  describe("Query messages") {
    describe("GetUserWithEmail") {
      it("should respond with failure if user doesn't exists") {
        val setup = defaultSetup

        setup.actor ! GetUserWithEmail("doesnt.exist@java.no")

        expectMsgClass(classOf[Failure[String]])
      }

      it("should forward message if user is exist") {
        val setup = defaultSetup
        val query: GetUserWithEmail = GetUserWithEmail(beerDuke.email.head.address)

        val cmd: HospesImportCmd = HospesImportCmd(beerDuke)
        setup.actor ! cmd
        setup.actor ! query

        setup.probe.expectMsg(cmd)
        setup.probe.expectMsgClass(classOf[GetUser])
      }
    }
  }
}

class UsrActor(id: UserId, actorRef: ActorRef) extends Actor {
  override def receive: Receive = {
    case m@_ => actorRef.forward(m)
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