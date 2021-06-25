package com.example.routes

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.example.db.MongoEntriesConnector
import com.example.directives.Requester
import com.example.utils.FileBasedResponses.{quotaOverflowedResponse, smallFileResponse}
import com.example.utils.Responses.{internalServerErrorResponse, okResponse}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object ClientDataRoute extends GlobalRoute {

  def apply(db: MongoEntriesConnector, auth: String, hostname: String)
           (system: ActorSystem[_], ec: ExecutionContext): Route =
    get {
      pathPrefix("get-single-record-structured") {
        parameter("filename".as[String]) { filename =>
          okResponse //TODO
        }
      } ~
        pathPrefix("get-file") {
          parameter("filename".as[String]) { filename =>
            onComplete(db.updateQuota(auth, 1)) {
              case Success(res) => res match {
                case Some(_) => smallFileResponse(filename)
                case None => quotaOverflowedResponse
              }
              case Failure(ex) =>
                system.log.error(s"Cannot update quota due to $ex")
                internalServerErrorResponse
            }
          }
        }
    }
}
