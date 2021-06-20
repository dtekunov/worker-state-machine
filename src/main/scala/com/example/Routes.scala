package com.example

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route

import scala.concurrent.{ExecutionContextExecutor, Future}
import com.example.UserRegistry._
import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout
import com.example.directives.{Admin, Client, Editor, Unknown, checkRequester}
import com.example.routes.{AdminApiRoute, ClientApiRoute, ClientDataRoute, EditorDataRoute, ServiceRoute}
import com.example.utils.Responses.{invalidClientEntityResponse, maxLimitResponse, notAcceptableResponse, pongResponse}
import com.typesafe.config.Config

/**
 * Main `routing`
 */
class Routes(registry: ActorRef[UserRegistry.Command])(implicit val system: ActorSystem[_]) {

  private val config: Config = system.settings.config
  private implicit val ec: ExecutionContextExecutor = system.executionContext
  private implicit val timeout: Timeout = Timeout.create(system.settings.config.getDuration("main.routes.ask-timeout"))


  def getUsers(): Future[Users] =
    registry.ask(GetUsers)
  def getUser(name: String): Future[GetUserResponse] =
    registry.ask(GetUser(name, _))
  def createUser(user: User): Future[ActionPerformed] =
    registry.ask(CreateUser(user, _))
  def deleteUser(name: String): Future[ActionPerformed] =
    registry.ask(DeleteUser(name, _))

  def addEntry(connectionString: String): Future[ActionPerformed] =
    ???

  def deleteEntry(connectionString: String): Future[ActionPerformed] =
    ???

  def isEntryExists(connectionString: String): Future[ActionPerformed] =
    ???

//  def getSingleDataChunk: Future[DataChunkResponse] =
//    ???

  val routes: Route =
    pathPrefix("service") {
      (headerValueByName("Client-Entity") & headerValueByName("Authorization")) {
        (rawRequester, auth) =>
          checkRequester(rawRequester) {
            case `Client` => notAcceptableResponse("Cannot access the following route with given Client-Entity")
            case `Editor` => notAcceptableResponse("Cannot access the following route with given Client-Entity")
            case `Admin` => AdminApiRoute(auth,registry)(system)
            case `Unknown` => invalidClientEntityResponse
          }
      }
    } ~ pathPrefix("data") {
      (headerValueByName("Client-Entity") & headerValueByName("Authorization")) {
        (rawRequester, auth) =>
          checkRequester(rawRequester) {
            case `Client` => ClientDataRoute(auth, registry)(system, ec)
            case `Editor` => EditorDataRoute(auth, registry)(system)
            case `Admin` => notAcceptableResponse("Cannot access the following route with given Client-Entity")
            case `Unknown` => invalidClientEntityResponse
          }
      }
    } ~ pathPrefix("service") {
      ServiceRoute(registry)(system, ec)
    }
}
