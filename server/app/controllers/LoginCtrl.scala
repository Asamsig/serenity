package controllers

import javax.inject.Inject

import auth.DefaultEnv
import com.mohiva.play.silhouette.api.Authenticator.Implicits._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.util.{Clock, Credentials}
import com.mohiva.play.silhouette.api.{LoginEvent, LogoutEvent, Silhouette}
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import controllers.helpers.RouterCtrl
import net.ceedubs.ficus.Ficus._
import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Request
import play.api.routing.Router.Routes
import play.api.routing.sird._
import services.UserIdentityService

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}

class LoginCtrl @Inject()(
    silhouette: Silhouette[DefaultEnv],
    credentialsProvider: CredentialsProvider,
    userIdentityService: UserIdentityService,
    configuration: Configuration,
    clock: Clock
)(implicit ec: ExecutionContext)
  extends RouterCtrl {

  override def withRoutes(): Routes = {
    case POST(p"/api/login") => login()
    case GET(p"/api/logout") => logout()
  }

  import silhouette.{UnsecuredAction, UserAwareAction}

  private val config = configuration.underlying
  private val expire: FiniteDuration =
    config.as[FiniteDuration]("silhouette.authenticator.rememberMe.authenticatorExpiry")

  private val idle: Option[FiniteDuration] = config.getAs[FiniteDuration](
    "silhouette.authenticator.rememberMe.authenticatorIdleTimeout"
  )

  def login() = UnsecuredAction.async(parse.json) { implicit request =>
    val (cred, rememberMe) = toCredentials
    credentialsProvider
      .authenticate(cred)
      .flatMap { loginInfo =>
        userIdentityService.retrieve(loginInfo).flatMap {
          case Some(user) =>
            silhouette.env.authenticatorService
              .create(loginInfo)
              .map {
                case authenticator if rememberMe =>
                  authenticator.copy(
                    expirationDateTime = clock.now + expire,
                    idleTimeout = idle
                  )
                case authenticator =>
                  authenticator
              }
              .flatMap { authenticator =>
                silhouette.env.eventBus.publish(LoginEvent(user, request))
                silhouette.env.authenticatorService.init(authenticator).map { v =>
                  Ok(Json.obj("token" -> v))
                }
              }

          case None =>
            Future.failed(new IdentityNotFoundException("Couldn't find user"))
        }
      }
      .recover {
        case pe: ProviderException =>
          Unauthorized(Json.obj("msg" -> "Invalid credentials"))
      }

  }

  def logout() = UserAwareAction.async { implicit request =>
    (for {
      user          <- request.identity
      authenticator <- request.authenticator
    } yield {
      silhouette.env.eventBus.publish(LogoutEvent(user, request))
      silhouette.env.authenticatorService.discard(authenticator, Ok)
    }).getOrElse(Future.successful(Ok))
  }

  private def toCredentials(
      implicit jsRequest: Request[JsValue]
  ): (Credentials, Boolean) = {
    val json     = jsRequest.body
    val usr      = (json \ "username").asOpt[String].getOrElse("")
    val pwd      = (json \ "password").asOpt[String].getOrElse("")
    val remember = (json \ "rememberMe").asOpt[Boolean].getOrElse(false)
    (Credentials(usr, pwd), remember)
  }

}
