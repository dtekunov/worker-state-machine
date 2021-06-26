package com.di.routes

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.di.db.MongoEntriesConnector
import com.di.directives.Requester
import com.di.utils.FileBasedResponses.{quotaOverflowedResponse, smallFileResponse}
import com.di.utils.Responses.{internalServerErrorResponse, okResponse}

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
