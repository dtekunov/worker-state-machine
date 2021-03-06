package com.di

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route

import scala.concurrent.{ExecutionContextExecutor, Future}
import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout
import com.di.db.MongoEntriesConnector
import com.di.db.MongoEntriesConnector.initiateDb
import com.di.directives._
import com.di.routes.{AdminApiRoute, ClientApiRoute, ClientDataRoute, EditorDataRoute, HealthcheckRoute}
import com.di.utils.Responses.{authenticationFailedResponse, deepPingResponse, hostnameNotFoundResponse, internalServerErrorResponse, invalidClientEntityResponse, invalidUrlResponse, maxLimitResponse, notAcceptableResponse, okResponse}
import com.typesafe.config.Config

/**
 * -- Main `routing`
 *
 * GET ~/healthcheck/~ -> HealthcheckRoute
 *
 * GET ~/service/~     -> ClientApiRoute | AdminApiRoute
 *
 * GET ~/data/~        -> ClientDataRoute | EditorDataRoute
 *
 * -- Checks `Authorization` and `Requester` headers, authorizes user
 *
 * @responses
 * <notAcceptableResponse>
 * <authenticationFailedResponse>
 * <internalServerErrorResponse>
 * <hostnameNotFoundResponse>
 * <invalidClientEntityResponse>
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
                case _ =>
                  system.log.error("Check auth failed, completing with 500")
                  internalServerErrorResponse
              }

            case `Admin` =>
              checkAuth(hostname, auth, db)(ec) {
                case `SuccessLogin` => AdminApiRoute(db, auth, hostname)(system, ec)
                case `AuthFailed` => authenticationFailedResponse
                case `HostnameNotFound` => hostnameNotFoundResponse
                case _ => internalServerErrorResponse
              }
            case _ =>
              system.log.error("Check auth failed, completing with 500")
              invalidClientEntityResponse
          }
        } ~
          pathPrefix("data") {
            val db = initiateDb(config)(ec)
            checkRequester(rawRequester) {
              case `Client` =>
                checkAuth(hostname, auth, db)(ec) {
                  case `SuccessLogin` => ClientDataRoute(db, auth, hostname)(system, ec)
                  case `AuthFailed` => authenticationFailedResponse
                  case `HostnameNotFound` => hostnameNotFoundResponse
                  case _ =>
                    system.log.error("Check auth failed, completing with 500")
                    internalServerErrorResponse
                }
              case `Editor` =>
                checkAuth(hostname, auth, db)(ec) {
                  case `SuccessLogin` => EditorDataRoute(db, auth, hostname)(system, ec)
                  case `AuthFailed` => authenticationFailedResponse
                  case `HostnameNotFound` => hostnameNotFoundResponse
                  case _ =>
                    system.log.error("Check auth failed, completing with 500")
                    internalServerErrorResponse
                }
              case `Admin` => notAcceptableResponse("Cannot access the following route with given Client-Entity")
              case _ => invalidClientEntityResponse
            }

            /**
             * Doc for this route is provided in Healthcheck route.scala
             */
          } ~
          pathPrefix("healthcheck") {
            pathPrefix(Remaining) {
              case remain if remain == "max-limit" =>
                maxLimitResponse(system.settings.config.getInt("main.max-limit"))
              case remain if remain == "ping" =>
                okResponse
              case remain if remain == "db_ping" =>
                checkRequester(rawRequester) {
                  case `Admin` =>
                    val db = initiateDb(config)(ec)
                    HealthcheckRoute(db, auth, hostname)(system, ec)
                  case _ => invalidClientEntityResponse
                }
              case _ => invalidUrlResponse
            }
          }
    }
}
