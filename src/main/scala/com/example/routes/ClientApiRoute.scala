package com.example.routes

import akka.actor.typed.ActorRef
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.Route
import com.example.UserRegistry.Command
import com.example.directives.Requester
import akka.http.scaladsl.server.Directives._
import com.example.db.UserLogsConnector

import scala.concurrent.ExecutionContext

object ClientApiRoute {

  def apply(requester: Requester, registry: ActorRef[Command])(implicit ec: ExecutionContext): Route = get {
    complete("TODO")

  }
}
