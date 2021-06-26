package com.di.routes

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.Route
import com.di.directives.Requester
import akka.http.scaladsl.server.Directives._
import com.di.db.MongoEntriesConnector
import com.di.jsonFormatters.JsonWriter.format
import com.di.utils.Responses.{entriesResponse, hostnameNotFoundResponse, internalServerErrorResponse}
import com.di.utils.mongoDocToEntry

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object ClientApiRoute extends GlobalRoute {

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
