package com.example

import akka.http.scaladsl.server.Directives.onComplete
import akka.http.scaladsl.server.{Directive0, Directive1}
import akka.http.scaladsl.server.directives.BasicDirectives.provide
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import com.example.db.MongoEntriesConnector
import com.example.utils.{tryOptionToOption, tryOptionWithAlternative}
import org.mongodb.scala.bson.collection.immutable.Document

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext}
import scala.util.{Failure, Success, Try}

package object directives {

  trait Requester
  case object Client  extends Requester
  case object Admin   extends Requester
  case object Editor  extends Requester
  case object Unknown extends Requester

  trait LoginStatus
  trait FailureLogin              extends LoginStatus

  case object SuccessLogin        extends LoginStatus
  case object HostnameNotFound    extends FailureLogin
  case object AuthFailed          extends FailureLogin
  case object InternalServerError extends FailureLogin

  /**
   * Checks, whether authentication entry for a given hostname exists in a database
   */
  def checkAuth(hostname: String, authToCheck: String, db: MongoEntriesConnector)
               (implicit ec: ExecutionContext): Directive1[LoginStatus]  = {

    val stubInternalErrorDoc = Document("_id" -> 0, "error" -> "internal")

    val result = tryOptionWithAlternative(
      Try(Await.result(db.getEntryByAuth(hostname), 5.seconds))
    )(stubInternalErrorDoc)

    result match {
      case Some(doc) if doc == stubInternalErrorDoc => provide(InternalServerError)
      case Some(doc) =>
        if (doc("auth_entry").asString().getValue == authToCheck)
          provide(SuccessLogin)
        else provide(AuthFailed)

      case None => provide(HostnameNotFound)
    }
  }

  /**
   * Formats data, provided from `Client-Entity` header and provides given object
   */
  def checkRequester(rawRequester: String): Directive1[Requester] = rawRequester.toLowerCase match {
    case requester if requester == "client" => provide(Client)
    case requester if requester == "admin" =>  provide(Admin)
    case requester if requester == "editor" => provide(Editor)
    case _ => provide(Unknown)
  }



}
