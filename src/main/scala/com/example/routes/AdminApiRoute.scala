package com.example.routes

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.example.db.{Entries, MongoEntriesConnector}
import com.example.directives.{Requester, checkAuth, checkRequester}
import com.example.jsonFormatters.JsonWriter.{format, formatEntrySeq}
import com.example.utils.Responses.{authMechanismIsNotWorkingResponse, authenticationFailedResponse, deepPingResponse, entriesResponse, hostnameNotFoundResponse, internalServerErrorResponse, maxLimitResponse, notAcceptableResponse, okResponse}
import com.example.utils.mongoDocToEntry

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object AdminApiRoute extends GlobalRoute {

  def apply(db: MongoEntriesConnector, auth: String, hostname: String)
           (system: ActorSystem[_], ec: ExecutionContext): Route =
    get {
      pathPrefix("get-admins-list") {
        onComplete(db.getAllEntries) {
          case Success(entries) => entries match {
            case Nil =>
              system.log.error("Auth mechanism is not working")
              authMechanismIsNotWorkingResponse("Admins list is empty, but auth works for some reason")
            case elems =>
              system.log.info("get-all admins list operation acquired")
              val entries = elems.map { doc =>
                Entries(doc("auth_entry").asString().getValue, doc("hostname").asString().getValue, isAdmin = true)
              }
              entriesResponse(formatEntrySeq(entries))
          }
          case Failure(ex) =>
            system.log.error(s"Cannot extract entries from db due to $ex")
            internalServerErrorResponse
        }
      } ~ pathPrefix("get-hostname-by-auth") {
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
  } ~ post {
    pathPrefix("add-admin") {
      parameter("auth", "hostname") {
        (auth, hostname) =>
        onComplete(db.insertSingleEntry(Entries(auth, hostname, isAdmin = true))) {
          case Success(Some(_)) => okResponse
          case _ =>
            system.log.error(s"Cannot insert entries to db")
            internalServerErrorResponse
        }
      }
    }
  } ~ delete {
    pathPrefix("delete-admin") {
      okResponse // TODO
    }
  }

}
