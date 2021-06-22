package com.example.routes

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives.{get, headerValueByName, pathPrefix}
import akka.http.scaladsl.server.Route
import com.example.utils.Responses.{authenticationFailedResponse, deepPingResponse, hostnameNotFoundResponse, internalServerErrorResponse, maxLimitResponse, notAcceptableResponse, okResponse}
import akka.http.scaladsl.server.Directives._
import com.example.db.MongoEntriesConnector
import com.example.directives.{Admin, AuthFailed, HostnameNotFound, InternalServerError, SuccessLogin, checkAuth, checkRequester}

import scala.concurrent.{ExecutionContext, Future}

object HealthcheckRoute extends GlobalRoute {

  /**
   * Service route for service healthcheck; db healthcheck; max limit of a server
   *
   * GET ~/healthcheck/max-limit -> <limitation of a server in application.conf>
   *
   * GET ~/healthcheck/ping -> <pong>
   *
   * GET ~/healthcheck/deep_ping + AUTH-> <db is ok>
   *
   * @responses
   * <notAcceptableResponse>
   * <pongResponse>
   * <maxLimitResponse>
   * <deepPingResponse>
   * <authenticationFailedResponse>
   * <internalServerErrorResponse>
   */
  def apply(db: MongoEntriesConnector, auth: String, hostname: String)
           (system: ActorSystem[_], ec: ExecutionContext): Route = get {
    checkAuth("admin", auth, db)(ec) {
      case `SuccessLogin` => deepPingResponse
      case `AuthFailed` => authenticationFailedResponse
      case `HostnameNotFound` => hostnameNotFoundResponse
      case _ => internalServerErrorResponse
    }
  }
}
