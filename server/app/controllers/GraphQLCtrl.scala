package controllers

import auth.DefaultEnv
import com.google.inject.Inject
import com.mohiva.play.silhouette.api.Silhouette
import controllers.helpers.RouterCtrl
import services.graphql.{
  PermissionEnforcerMiddleware,
  SchemaDefinition,
  GraphQlContext,
  SecurityException
}
import models.user.User
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.mvc.{Action, Controller}
import play.api.routing.Router.Routes
import play.api.routing.sird._
import repositories.view.UserRepository
import sangria.ast.Document
import sangria.execution._
import sangria.parser.{QueryParser, SyntaxError}
import sangria.renderer.SchemaRenderer

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class GraphQLCtrl @Inject()(
    userRepository: UserRepository,
    silhouette: Silhouette[DefaultEnv]
)(implicit ec: ExecutionContext)
    extends RouterCtrl
    with Controller {

  import sangria.marshalling.playJson._
  import silhouette.UserAwareAction

  override def withRoutes(): Routes = {
    case GET(p"/api/render-schema") =>
      renderSchema
    case GET(
        p"/api/graphql"
          ? q"query=$query" & q_o"operation=$operation" & q_o"variables=$variables"
        ) =>
      graphqlRequest(query, operation, variables)
    case POST(p"/api/graphql") =>
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
      parseAndExecute(
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
        case JsString(vars) => Some(parseVariables(vars))
        case obj: JsObject  => Some(obj)
        case _              => None
      }.getOrElse(Json.obj())

      parseAndExecute(request.identity, query, operation, variables)
    }

  private def parseAndExecute(
      user: Option[User],
      query: String,
      operation: Option[String],
      variables: JsObject
  ) = {
    QueryParser.parse(query) match {
      case Success(queryAst) =>
        executeGraphQLQuery(queryAst, variables, operation, user)
      case Failure(error: SyntaxError) =>
        Future.successful(BadRequest(Json.obj("error" -> error.getMessage)))
      case Failure(t) =>
        Future.successful(InternalServerError(Json.obj("error" -> t.getMessage)))
    }
  }

  private def parseVariables(variables: String) =
    if (variables.trim == "" || variables.trim == "null") {
      Json.obj()
    } else {
      Json.parse(variables).as[JsObject]
    }

  private val errorHandler: Executor.ExceptionHandler = {
    case (_, SecurityException(message)) => HandledException(message)
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
      userContext = GraphQlContext(user, userRepository),
      operationName = operation,
      variables = variables,
      middleware = PermissionEnforcerMiddleware :: Nil,
      exceptionHandler = errorHandler
    )

    executor.map(v => Ok(v)).recover {
      case error: QueryAnalysisError => BadRequest(error.resolveError)
      case error: ErrorWithResolver  => InternalServerError(error.resolveError)
    }
  }

}
