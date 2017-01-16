package controller

import javax.inject.{Inject, Named, Singleton}

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import play.api.libs.json._
import play.api.mvc.{Action, Result, Results}
import serenity.hospesimport.ImportFromHospes
import serenity.hospesimport.model.{MembershipJson, PersonJson}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success, Try}
import play.api.mvc.BodyParsers.parse

@Singleton
class HospesImportCtrl @Inject()(
    @Named("UserManagerActor") userManagerActor: ActorRef)(
    implicit ec: ExecutionContext) extends HospesImportCtrlFormats {

  implicit val timeout: Timeout = 120.seconds

  def importData() = Action.async(parse.json(Int.MaxValue)) {
    request =>
      request.body match {
        case value: JsObject =>
          Future {
            (for {
              p <- (value \ "persons").asOpt[List[PersonJson]]
              m <- (value \ "memberships").asOpt[List[MembershipJson]]
            } yield executeImport(p, m))
                .getOrElse(Results.BadRequest("Illegal input structure"))
          }.recover { case e => e.printStackTrace();Results.InternalServerError(e.getMessage) }
        case _ => Future.successful(Results.BadRequest("Missing data or wrong data"))
      }
  }

  def executeImport(pJson: List[PersonJson], mJson: List[MembershipJson]): Result = {
    val result: Future[List[Any]] = Future.sequence(ImportFromHospes.apply(mJson, pJson).map(userManagerActor ? _))
    val (sCount, fCount) = Await.result(result, timeout.duration).foldLeft((0, 0)) { case ((s, f), msg) => msg match {
      case Success(_) => (s + 1, 0)
      case Failure(m) => (0, f + 1)
      case "User created" => (s + 1, f)
      case m => (s, f)
    }
    }
    Results.Ok(s"Input: ${pJson.size}\nSuccesses: $sCount\nFailures: $fCount")
  }

}

trait HospesImportCtrlFormats {
  implicit val bigIntFormat = new Format[BigInt] {
    override def reads(json: JsValue): JsResult[BigInt] = json.validate[String].flatMap { s =>
      Try(BigInt(s)).map(v => JsSuccess(v)).getOrElse(JsError(s"$s is not a number"))
    }

    override def writes(o: BigInt): JsValue = JsString(o.toString)
  }
  implicit val jsonPersonFormat: Format[PersonJson] = Json.format[PersonJson]
  implicit val jsonMembershipFormat: Format[MembershipJson] = Json.format[MembershipJson]
}
