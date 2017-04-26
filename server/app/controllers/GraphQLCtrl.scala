package controllers

import auth.DefaultEnv
import com.google.inject.Inject
import com.mohiva.play.silhouette.api.Silhouette
import controllers.helpers.RouterCtrl
import models.graphql.{Context, SchemaDefinition}
import models.user.User
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.mvc.{Action, Controller}
import play.api.routing.Router.Routes
import play.api.routing.sird._
import sangria.ast.Document
import sangria.execution.{ErrorWithResolver, Executor, QueryAnalysisError}
import sangria.parser.{QueryParser, SyntaxError}
import sangria.renderer.SchemaRenderer
import services.UserService

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class GraphQLCtrl @Inject()(
    silhouette: Silhouette[DefaultEnv],
    userService: UserService
)(implicit ec: ExecutionContext)
    extends RouterCtrl
    with Controller {

  import silhouette.UserAwareAction

  override def withRoutes(): Routes = {
    case GET(p"/render-schema") =>
      renderSchema
    case GET(
        p"/graphql"
          ? q"query=$query" & q_o"operation=$operation" & q_o"variables=$variables"
        ) =>
      graphqlRequest(query, operation, variables)
    case POST(p"/graphql") =>
      graphqlBody()
  }

  def renderSchema = Action {
    Ok(SchemaRenderer.renderSchema(SchemaDefinition.SerenitySchema))
  }

  def graphqlRequest(
      query: String,
      operation: Option[String],
      variables: Option[String]
  ) =
    UserAwareAction.async { request =>
      parseAndExecure(
        request.identity,
        query,
        operation,
        parseVariables(variables.getOrElse(""))
      )
    }

  def graphqlBody() =
    UserAwareAction.async(parse.json) { request =>
      val query     = (request.body \ "query").as[String]
      val operation = (request.body \ "operationName").asOpt[String]
      val variables = (request.body \ "variables").toOption.flatMap {
        case JsString(vars) ⇒ Some(parseVariables(vars))
        case obj: JsObject  ⇒ Some(obj)
        case _              ⇒ None
      }.getOrElse(Json.obj())

      parseAndExecure(request.identity, query, operation, variables)
    }

  private def parseAndExecure(
      user: Option[User],
      query: String,
      operation: Option[String],
      variables: JsObject
  ) = {
    QueryParser.parse(query) match {
      case Success(queryAst) ⇒
        executeGraphQLQuery(queryAst, variables, operation, user)
      case Failure(error: SyntaxError) ⇒
        Future.successful(BadRequest(Json.obj("error" → error.getMessage)))
    }
  }

  private def parseVariables(variables: String) =
    if (variables.trim == "" || variables.trim == "null") {
      Json.obj()
    } else {
      Json.parse(variables).as[JsObject]
    }

  private def executeGraphQLQuery(
      query: Document,
      variables: JsObject,
      operation: Option[String],
      user: Option[User]
  ) = {

    val executor = Executor.execute(
      schema = SchemaDefinition.SerenitySchema,
      queryAst = query,
      userContext = Context(user),
      operationName = operation,
      variables = variables
    )

    executor.map(Ok(_)).recover {
      case error: QueryAnalysisError ⇒ BadRequest(error.resolveError)
      case error: ErrorWithResolver  ⇒ InternalServerError(error.resolveError)
    }
  }

}
