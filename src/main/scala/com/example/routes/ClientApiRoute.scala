package com.example.routes

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.Route
import com.example.directives.Requester
import akka.http.scaladsl.server.Directives._
import com.example.db.MongoEntriesConnector
import com.example.jsonFormatters.JsonWriter.format
import com.example.utils.Responses.{entriesResponse, hostnameNotFoundResponse, internalServerErrorResponse}
import com.example.utils.mongoDocToEntry

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object ClientApiRoute {

  def apply(db: MongoEntriesConnector, auth: String, hostname: String)
           (system: ActorSystem[_], ec: ExecutionContext): Route =
    get {
      pathPrefix("account") {
        onComplete(db.getEntryByAuth(auth)) {
          case Success(maybeDoc) =>
            maybeDoc match {
              case Some(doc) => entriesResponse(format(mongoDocToEntry(doc)))
              case None => hostnameNotFoundResponse
            }
          case Failure(ex) =>
            system.log.error(s"Cannot extract entries from db due to $ex")
            internalServerErrorResponse
        }
      }
  }
}
