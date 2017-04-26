package services

import javax.inject.{Inject, Named}

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import models.hospes.{MembershipJson, PersonJson}
import models.user.Email
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import repositories.eventsource.users.UserWriteProtocol.{
  HospesImportCmd,
  HospesMembership,
  HospesUser
}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}

class HospesImportService @Inject()(
    @Named("UserManagerActor") userManagerActor: ActorRef
) {

  val logger                    = Logger(classOf[HospesImportService])
  implicit val timeout: Timeout = 120.seconds

  def executeImport(pJson: List[PersonJson], mJson: List[MembershipJson]): (Int, Int) = {
    logger.info("Starting importing users")
    val result: Future[List[Any]] = Future.sequence(
      findUniqueUsersWithMembership(mJson, pJson).map(userManagerActor ? _)
    )
    Await.result(result, timeout.duration).foldLeft((0, 0)) {
      case ((s, f), msg) =>
        msg match {
          case Success(_) =>
            (s + 1, 0)
          case Failure(m) =>
            logger.info(s"import of user with error ${m.getMessage}", m)
            (0, f + 1)
          case "User created" =>
            (s + 1, f)
          case m =>
            logger.info(s"Unhandled response $m")
            (s, f)
        }
    }
  }

  def findUniqueUsersWithMembership(
      mJson: List[MembershipJson],
      pJson: List[PersonJson]
  ): List[HospesImportCmd] = {
    val fm = mJson
      .filter(_.member_person_id.isDefined)
      .groupBy(_.member_person_id.getOrElse(0))
      .mapValues(_.map(m => HospesMembership(m.id, m.year)))

    val mId = fm.keys.toSet

    val fp = pJson
    //      .filter(_.validated)
      .filter(p => mId.contains(p.id))

    def cleanupPhone(v: Option[String]): Option[String] =
      v.map(_.replaceAll(" ", "").replace("+47", ""))

    def nonEmpty(v: Option[String]): Boolean = v match {
      case Some("") => false
      case None     => false
      case _        => true
    }

    def toPerson(ps: List[PersonJson]): HospesImportCmd = {
      val sp: List[PersonJson] = ps.sortBy(_.id).reverse
      val ids                  = sp.map(_.id).toSet
      val memberships          = fm.filter(m => ids.contains(m._1)).flatMap(_._2).toSet

      HospesImportCmd(
        HospesUser(
          sp.map(_.id),
          Email(sp.head.email, sp.head.validated) :: sp.tail
            .map(m => Email(m.email, m.validated)),
          sp.head.firstname,
          sp.head.lastname,
          sp.head.address,
          sp.head.phonenumber,
          sp.head.password_pw,
          sp.head.password_slt,
          memberships
        )
      )
    }

    val groupedByName = fp
      .filter(p => nonEmpty(p.firstname) || nonEmpty(p.lastname))
      .groupBy(
        p =>
          s"${p.firstname.getOrElse("").toLowerCase} : ${p.lastname.getOrElse("").toLowerCase}"
      )
      .filter(_._2.size > 1)

    val (identifiedPhone, others) =
      groupedByName.partition(_._2.map(p => cleanupPhone(p.phonenumber)).toSet.size == 1)

    val resId: Set[Int] = groupedByName.values.flatMap(_.map(_.id)).toSet

    val res = fp.filter(p => !resId.contains(p.id)).map(f => toPerson(List(f))) :::
      identifiedPhone.map(p => toPerson(p._2)).toList :::
      others.map(p => toPerson(p._2)).toList
    logger.info(s"Import filtered to ${res.size} users")
    res
  }

}
