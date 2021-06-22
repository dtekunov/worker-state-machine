package com.example.routes

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.Route
import com.example.db.MongoEntriesConnector
import com.example.directives.Requester

import scala.concurrent.ExecutionContext

object EditorDataRoute extends GlobalRoute {

  def apply(db: MongoEntriesConnector, auth: String, hostname: String)
           (system: ActorSystem[_], ec: ExecutionContext): Route = { complete("TODO") }

}
