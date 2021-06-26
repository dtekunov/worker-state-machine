package com.di.routes

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.Route
import com.di.db.MongoEntriesConnector
import com.di.directives.Requester

import scala.concurrent.ExecutionContext

object EditorDataRoute extends GlobalRoute {

  def apply(db: MongoEntriesConnector, auth: String, hostname: String)
           (system: ActorSystem[_], ec: ExecutionContext): Route = { complete("TODO") }

}
