package com.example.routes

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives.{get, headerValueByName, pathPrefix}
import akka.http.scaladsl.server.Route
import com.example.utils.Responses.{maxLimitResponse, notAcceptableResponse, pongResponse}
import akka.http.scaladsl.server.Directives._
import com.example.UserRegistry.Command
import com.example.directives.{Admin, checkRequester}

import scala.concurrent.ExecutionContext

object ServiceRoute {

  def apply(registry: ActorRef[Command])(implicit system: ActorSystem[_], ec: ExecutionContext): Route = get {
    pathPrefix(Remaining) {
      case remain if remain == "max-limit" =>
        maxLimitResponse(system.settings.config.getInt("main.max-limit"))
      case remain if remain == "ping" =>
        pongResponse
      case remain if remain == "deep_ping" =>
        (headerValueByName("Client-Entity") & headerValueByName("Authorization")) {
          (rawRequester, auth) =>
            checkRequester(rawRequester) {
              case `Admin` => pongResponse //TODO
              case _ => notAcceptableResponse("Cannot access the following route with given Client-Entity")
            }
        }
      case _ =>
        notAcceptableResponse("Service route only supports `max-limit`, `pong` and `deep-ping`")
    }
  }
}
