package com.example.routes

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.example.db.MongoEntriesConnector
import com.example.directives.Requester
import com.example.utils.FileBasedResponses.smallFileResponse
import com.example.utils.Responses.okResponse

import scala.concurrent.ExecutionContext

object ClientDataRoute extends GlobalRoute {

  def apply(db: MongoEntriesConnector, auth: String, hostname: String)
           (system: ActorSystem[_], ec: ExecutionContext): Route =
    get {
      pathPrefix("get-single-record-structured") {
        parameter("filename".as[String]) { filename =>
          okResponse //TODO
        }
      } ~ pathPrefix("get-file") {
        parameter("filename".as[String]) {
          filename => smallFileResponse(filename)
          }
        }
      }
}
