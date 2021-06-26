package com.di.routes

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.server.Route
import com.di.db.MongoEntriesConnector

import scala.concurrent.ExecutionContext

trait GlobalRoute {

  def apply(db: MongoEntriesConnector, auth: String, hostname: String)
           (system: ActorSystem[_], ec: ExecutionContext): Route
}
