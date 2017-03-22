package controller

import javax.inject.{Inject, Named, Singleton}

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import auth.{DefaultEnv, WithRole}
import com.mohiva.play.silhouette.api.Silhouette
import controller.helpers.RouterCtrl
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc.BodyParsers.parse
import play.api.mvc.{Result, Results}
import play.api.routing.Router
import play.api.routing.sird._
import serenity.hospesimport.ImportFromHospes
import serenity.hospesimport.model.{MembershipJson, PersonJson}
import serenity.users.domain.AdminRole

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}

@Singleton
class HospesImportCtrl @Inject()(
    @Named("UserManagerActor") userManagerActor: ActorRef,
    silhouette: Silhouette[DefaultEnv]
) extends HospesImportCtrlFormats with RouterCtrl {

  override def withRoutes(): Router.Routes = {
    case POST(p"/api/hospes/import") => importData()
  }

  import silhouette.SecuredAction

  implicit val timeout: Timeout = 120.seconds

  def importData() = SecuredAction(WithRole(AdminRole))
      .async(parse.json(Int.MaxValue)) { request =>
        request.body match {
          case value: JsObject =>
            Future {
              (for {
                p <- (value \ "persons").asOpt[List[PersonJson]]
                m <- (value \ "memberships").asOpt[List[MembershipJson]]
              } yield executeImport(p, m))
                  .getOrElse(Results.BadRequest("Illegal input structure"))
            }.recover { case e => e.printStackTrace(); Results.InternalServerError(e.getMessage) }
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
    override def reads(json: JsValue): JsResult[BigInt] = json match {
      case JsNumber(n) =>
        JsSuccess(n.toBigInt())
      case JsString(s) =>
        Try(BigInt(s))
            .map(v => JsSuccess(v))
            .getOrElse(JsError(s"$s is not a number"))
      case _ =>
        JsError("Not parsable to BitInt")
    }

    override def writes(o: BigInt): JsValue = JsString(o.toString)
  }
  implicit val jsonPersonFormat: Format[PersonJson] = Json.format[PersonJson]
  implicit val jsonMembershipFormat: Format[MembershipJson] = Json.format[MembershipJson]
}
