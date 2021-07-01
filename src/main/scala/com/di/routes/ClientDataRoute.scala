package com.di.routes

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.di.db.MongoEntriesConnector
import com.di.directives.Requester
import com.di.routes.ClientApiRoute.recordUserAction
import com.di.utils.FileBasedResponses.{quotaOverflowedResponse, getFileResponse}
import com.di.utils.Responses.{internalServerErrorResponse, okResponse}
import com.di.utils.{ActionType, mongoDocToEntry}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object ClientDataRoute extends GlobalRoute {

  def apply(db: MongoEntriesConnector, auth: String, hostname: String)
           (implicit system: ActorSystem[_], ec: ExecutionContext): Route =
    get {
      pathPrefix("get-single-record-structured") {
        parameter("filename".as[String]) { filename =>
          okResponse //TODO
        }
      } ~
        pathPrefix("get-file") {
          parameter("filename".as[String]) { filename =>
            onComplete(db.updateQuota(hostname, 1)) {
              case Success(Some(_)) =>
                recordUserAction(hostname, db, ActionType("get-file"))
                getFileResponse(filename)
              case Success(None) => quotaOverflowedResponse
              case Failure(ex) =>
                system.log.error(s"Cannot update quota due to $ex")
                internalServerErrorResponse
            }
          }
        }
    }
}
