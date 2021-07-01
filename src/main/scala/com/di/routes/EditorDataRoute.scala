package com.di.routes

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.directives.FileInfo
import com.di.db.MongoEntriesConnector
import com.di.directives.Requester
import com.di.utils.FileBasedResponses.fileUploadedResponse
import com.di.utils.Responses.okResponse

import java.io.File
import scala.concurrent.ExecutionContext

object EditorDataRoute extends GlobalRoute {

  def apply(db: MongoEntriesConnector, auth: String, hostname: String)
           (implicit system: ActorSystem[_], ec: ExecutionContext): Route = {

    def tempDestination(fileInfo: FileInfo): File = File.createTempFile(fileInfo.fileName, ".tmp")

    get {
      pathPrefix("exists") {
        parameter("filename") { filename =>
          okResponse //TODO
        }
      }
    } ~
      delete {
        okResponse //TODO
      } ~
      storeUploadedFile("csv", tempDestination) {
        case (metadata, file) =>
          fileUploadedResponse(metadata.fileName)
    } ~
      storeUploadedFile("txt", tempDestination) {
        case (metadata, file) =>
          fileUploadedResponse(metadata.fileName)
    }
  }
}
