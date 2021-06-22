package com.example.routes

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.Route
import com.example.directives.Requester

object EditorDataRoute {

  def apply(auth: String)(implicit system: ActorSystem[_]): Route = { complete("TODO") }

}
