package com.example.routes

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.example.directives.Requester
import com.example.utils.Responses.pongResponse

import scala.concurrent.ExecutionContext

object ClientDataRoute {

  def apply(auth: String)
           (implicit system: ActorSystem[_], ec: ExecutionContext): Route =
    get {
      pathPrefix("get-single-record-structured") {
        parameter("filename".as[String]) { filename =>
          pongResponse //TODO
        }
      } ~ pathPrefix("get-records") {
        parameter("filename".as[String], "num".as[Int]) { (filename, num) =>
          pongResponse //TODO
        }
      } ~ pathPrefix("get-full") {
        parameter("filename".as[String]) { filename =>
          pongResponse //TODO
        }
      }
    }
}
