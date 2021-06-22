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
import com.example.db.MongoEntriesConnector.initiateDb
import com.example.directives._
import com.example.routes.{AdminApiRoute, ClientApiRoute, ClientDataRoute, EditorDataRoute, HealthcheckRoute}
import com.example.utils.Responses.{authenticationFailedResponse, deepPingResponse, hostnameNotFoundResponse, internalServerErrorResponse, invalidClientEntityResponse, maxLimitResponse, notAcceptableResponse, okResponse}
import com.typesafe.config.Config

/**
 * Main `routing`
 */
class Routes()(implicit val system: ActorSystem[_]) {

  private val config: Config = system.settings.config
  private implicit val ec: ExecutionContextExecutor = system.executionContext
  private implicit val timeout: Timeout = Timeout.create(system.settings.config.getDuration("main.routes.ask-timeout"))

  val routes: Route =
    (headerValueByName("Client-Entity") &
      headerValueByName("Authorization") &
      headerValueByName("Host")) {
      (rawRequester, auth, hostname) =>
        pathPrefix("service") {
          val db = initiateDb(config)(ec)
          checkRequester(rawRequester) {
            case `Client` =>
              checkAuth(hostname, auth, db)(ec) {
                case `SuccessLogin` => ClientApiRoute(db, auth, hostname)(system, ec)
                case `AuthFailed` => authenticationFailedResponse
                case `HostnameNotFound` => hostnameNotFoundResponse
                case _ => internalServerErrorResponse
              }

            case `Admin` =>
              checkAuth(hostname, auth, db)(ec) {
                case `SuccessLogin` => AdminApiRoute(db, auth, hostname)(system, ec)
                case `AuthFailed` => authenticationFailedResponse
                case `HostnameNotFound` => hostnameNotFoundResponse
                case _ => internalServerErrorResponse
              }
            case _ => invalidClientEntityResponse
          }

        } ~ pathPrefix("data") {
          val db = initiateDb(config)(ec)
          checkRequester(rawRequester) {
            case `Client` =>
              checkAuth(hostname, auth, db)(ec) {
                case `SuccessLogin` => ClientDataRoute(db, auth, hostname)(system, ec)
                case `AuthFailed` => authenticationFailedResponse
                case `HostnameNotFound` => hostnameNotFoundResponse
                case _ => internalServerErrorResponse
              }

            case `Editor` => EditorDataRoute(db, auth, hostname)(system, ec)
            case `Admin` => notAcceptableResponse("Cannot access the following route with given Client-Entity")
            case _ => invalidClientEntityResponse
          }
          /**
           * Doc for this route is provided in Healthcheck route.scala
           */
        } ~ pathPrefix("healthcheck") {
          pathPrefix(Remaining) {
            case remain if remain == "max-limit" =>
              maxLimitResponse(system.settings.config.getInt("main.max-limit"))
            case remain if remain == "ping" =>
              okResponse
            case remain if remain == "deep_ping" =>
              checkRequester(rawRequester) {
                case `Admin` =>
                  val db = initiateDb(config)(ec)
                  HealthcheckRoute(db, auth, hostname)(system, ec)
                case _ => invalidClientEntityResponse
              }
          }
        }
    }
}