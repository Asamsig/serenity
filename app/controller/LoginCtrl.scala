package controller

import javax.inject.Inject

import auth.{DefaultEnv, UserIdentityService}
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.util.Credentials
import com.mohiva.play.silhouette.api.{LoginEvent, LogoutEvent, Silhouette}
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Controller, Request}

import scala.concurrent.Future

class LoginCtrl @Inject()(
    silhouette: Silhouette[DefaultEnv],
    credentialsProvider: CredentialsProvider,
    userIdentityService: UserIdentityService
) extends Controller {

  def login() = silhouette.UnsecuredAction.async(parse.json) { implicit request =>
    val (cred, rememberMe) = toCredentials
    credentialsProvider.authenticate(cred).flatMap { loginInfo =>
      userIdentityService.retrieve(loginInfo).flatMap {
        case Some(user) =>
          silhouette.env.authenticatorService.create(loginInfo).map {
            case authenticator if rememberMe =>
              authenticator //todo
            case authenticator =>
              authenticator
          }.flatMap { authenticator =>
            silhouette.env.eventBus.publish(LoginEvent(user, request))
            silhouette.env.authenticatorService.init(authenticator).map { v =>
              Ok(Json.obj("token" -> v))

            }
          }

        case None =>
          Future.failed(new IdentityNotFoundException("Couldn't find user"))
      }
    }.recover {
      case pe: ProviderException => Unauthorized(Json.obj("msg" -> "Invalid credentials"))
    }

  }

  def logout() = silhouette.UserAwareAction.async { implicit request =>
    (for {
      user <- request.identity
      authenticator <- request.authenticator
    } yield {
      silhouette.env.eventBus.publish(LogoutEvent(user, request))
      silhouette.env.authenticatorService.discard(authenticator, Ok)
    }).getOrElse(Future.successful(Ok))
  }

  private def toCredentials(implicit request: Request[JsValue]): (Credentials, Boolean) = {
    val json = request.body
    val usr = (json \ "username").asOpt[String].getOrElse("")
    val pwd = (json \ "password").asOpt[String].getOrElse("")
    val remember = (json \ "rememberMe").asOpt[Boolean].getOrElse(false)
    (Credentials(usr, pwd), remember)
  }

}
