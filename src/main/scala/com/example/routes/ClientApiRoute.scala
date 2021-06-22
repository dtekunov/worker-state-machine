package com.example.routes

import akka.actor.typed.ActorRef
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.Route
import com.example.directives.Requester
import akka.http.scaladsl.server.Directives._

import scala.concurrent.ExecutionContext

object ClientApiRoute {

  def apply(requester: Requester)(implicit ec: ExecutionContext): Route = get {
    complete("TODO")

  }
}
