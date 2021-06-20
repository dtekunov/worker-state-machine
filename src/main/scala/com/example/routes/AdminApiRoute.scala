package com.example.routes

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.Route
import com.example.UserRegistry.Command
import com.example.directives.Requester

object AdminApiRoute {

  def apply(auth: String, registry: ActorRef[Command])(system: ActorSystem[_]): Route =
  {
    complete("TODO")
  }

}
