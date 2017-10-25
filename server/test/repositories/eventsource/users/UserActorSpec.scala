package repositories.eventsource.users

import akka.actor.ActorRef
import akka.actor.Status.{Failure, Success}
import helpers.akka.{AkkaConfig, AkkaSuite}
import models.user.{Email, UserId}
import repositories.eventsource.users.UserReadProtocol._
import repositories.eventsource.users.UserWriteProtocol.{
  HospesImportCmd,
  HospesUser,
  ValidationFailed
}
import repositories.view.memory.MemoryUserRepository

class UserActorSpec extends AkkaSuite("UserActorSpec", AkkaConfig.inMemoryPersistence()) {
  val hospesUser: HospesUser = HospesUser(
    List(),
    List(Email("example@java.no", validated = true)),
    None,
    None,
    None,
    None,
    "pw",
    "salt",
    Set()
  )

  describe("Persist and Query") {

    it("should handle hospes imports") {
      val repo                = new MemoryUserRepository()
      val userActor: ActorRef = system.actorOf(UserActor(repo, UserId.generate()))
      userActor ! HospesImportCmd(hospesUser)

      expectMsgClass(classOf[Success])
    }

    it("should handle fail when importing same user") {
      val repo                = new MemoryUserRepository()
      val userActor: ActorRef = system.actorOf(UserActor(repo, UserId.generate()))

      userActor ! HospesImportCmd(hospesUser)
      expectMsgClass(classOf[Success])

      userActor ! HospesImportCmd(hospesUser)

      expectMsg(Failure(ValidationFailed("User exist")))
    }

    it("should handle query for user") {
      val repo                = new MemoryUserRepository()
      val userId: UserId      = UserId.generate()
      val userActor: ActorRef = system.actorOf(UserActor(repo, userId))
      userActor ! HospesImportCmd(hospesUser)
      expectMsgClass(classOf[Success])

      userActor ! GetUser(userId)
      expectMsgAnyClassOf(classOf[UserResponse])
    }

    it("should read up state after shutdown") {
      val repo                      = new MemoryUserRepository()
      val userId: UserId            = UserId.generate()
      val originUserActor: ActorRef = system.actorOf(UserActor(repo, userId))
      originUserActor ! HospesImportCmd(hospesUser)
      expectMsgClass(classOf[Success])

      system.stop(originUserActor)

      val restoredUserActor: ActorRef = system.actorOf(UserActor(repo, userId))

      restoredUserActor ! GetUser(userId)
      expectMsgAnyClassOf(classOf[UserResponse])
    }

    it("should return BasicAuth when credentials exists") {
      val repo                = new MemoryUserRepository()
      val plainPwd            = "myS3cr3tPwd"
      val userActor: ActorRef = system.actorOf(UserActor(repo, UserId.generate()))
      val usr                 = hospesUser
      userActor ! HospesImportCmd(usr)

      expectMsgClass(classOf[Success])

      userActor ! GetUserCredentials(usr.email.head.address)

      expectMsgClass(classOf[UserCredentialsResponse])
    }

    it("should return CredentialsNotFound when credentials doesn't exists ") {
      val repo                = new MemoryUserRepository()
      val plainPwd            = "myS3cr3tPwd"
      val userActor: ActorRef = system.actorOf(UserActor(repo, UserId.generate()))
      val usr                 = hospesUser
      userActor ! HospesImportCmd(usr)

      expectMsgClass(classOf[Success])

      userActor ! GetUserCredentials("some@random.user")

      expectMsg(CredentialsNotFound)
    }
  }

}
