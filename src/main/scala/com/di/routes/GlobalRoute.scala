package com.di.routes

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.server.Route
import com.di.db.{MongoEntriesConnector, UserLogs}
import com.di.utils.ActionType

import java.time.LocalDateTime
import java.util.UUID
import scala.concurrent.ExecutionContext

trait GlobalRoute {

  def apply(db: MongoEntriesConnector, auth: String, hostname: String)
           (implicit system: ActorSystem[_], ec: ExecutionContext): Route

  def recordUserAction(hostname: String, db: MongoEntriesConnector, maybeAction: Option[ActionType])
                      (implicit ec: ExecutionContext, system: ActorSystem[_]): Unit = {
    maybeAction match {
      case Some(action) =>

        val logToInsert = UserLogs(
          UUID.randomUUID().toString,
          hostname,
          LocalDateTime.now()
        )

        db.insertSingleUserLogs(logToInsert).foreach {
          case Some(_) =>
            system.log.debug(s"Acquired ${action.name} action")
          case None =>
            system.log.error(s"Client action failed")
        }
      case None =>
        system.log.error(s"No such client action: $maybeAction")
    }
  }
}
