package com.example

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route

import scala.concurrent.{ExecutionContextExecutor, Future}
import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout
import com.example.db.MongoEntriesConnector
import com.example.directives._
import com.example.routes.{AdminApiRoute, ClientApiRoute, ClientDataRoute, EditorDataRoute, ServiceRoute}
import com.example.utils.Responses.{authenticationFailedResponse, deepPingResponse, internalServerErrorResponse, invalidClientEntityResponse, maxLimitResponse, notAcceptableResponse, okResponse}
import com.typesafe.config.Config

/**
 * Main `routing`
 */
class Routes()(implicit val system: ActorSystem[_]) {

  private val config: Config = system.settings.config
  private implicit val ec: ExecutionContextExecutor = system.executionContext
  private implicit val timeout: Timeout = Timeout.create(system.settings.config.getDuration("main.routes.ask-timeout"))

  val routes: Route =
    pathPrefix("service") {
      (headerValueByName("Client-Entity") &
        headerValueByName("Authorization") &
        headerValueByName("Host")) {
        (rawRequester, auth, hostname) =>
          checkRequester(rawRequester) {
            case `Admin` => {
              val dbName = config.getString("main.db.name")
              val db = new MongoEntriesConnector(dbName)
              checkAuth(hostname, auth, db)(ec) {
                case `SuccessLogin` => AdminApiRoute(db, auth)(system, ec)
                case `AuthFailed` => authenticationFailedResponse
                case _ => internalServerErrorResponse
              }
            }
            case `Unknown` => invalidClientEntityResponse
            case _ => notAcceptableResponse("Cannot access the following route with given Client-Entity")
          }
      }
    } ~ pathPrefix("data") {
      (headerValueByName("Client-Entity") & headerValueByName("Authorization")) {
        (rawRequester, auth) =>
          checkRequester(rawRequester) {
            case `Client` => ClientDataRoute(auth)(system, ec)
            case `Editor` => EditorDataRoute(auth)(system)
            case `Admin` => notAcceptableResponse("Cannot access the following route with given Client-Entity")
            case `Unknown` => invalidClientEntityResponse
          }
      }
    } ~ pathPrefix("healthcheck") {
      ServiceRoute(system, ec)
    }
}
