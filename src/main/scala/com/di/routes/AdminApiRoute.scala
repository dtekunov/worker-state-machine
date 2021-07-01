package com.di.routes

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.di.db.{Entries, MongoEntriesConnector}
import com.di.jsonFormatters.JsonWriter.{format, formatEntrySeq}
import com.di.utils.Responses._
import com.di.utils.mongoDocToEntry

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object AdminApiRoute extends GlobalRoute {

  def apply(db: MongoEntriesConnector, auth: String, hostname: String)
           (implicit system: ActorSystem[_], ec: ExecutionContext): Route =
    get {
      pathPrefix("get-admins-list") {
        onComplete(db.getAllEntries) {
          case Success(entries) => entries match {
            case Nil =>
              system.log.error("Auth mechanism is not working")
              authMechanismIsNotWorkingResponse("Admins list is empty, but auth works for some reason")
            case elems =>
              system.log.info("get-all admins list operation acquired")
              val entries = elems.map { doc => mongoDocToEntry(doc) }
              entriesResponse(formatEntrySeq(entries))
          }
          case Failure(ex) =>
            system.log.error(s"Cannot extract entries from db due to $ex")
            internalServerErrorResponse
        }
      } ~
        pathPrefix("get-entry-by-auth") {
          parameter("authToFind") { authToFind =>
            onComplete(db.getEntryByAuth(authToFind)) {
              case Success(maybeDoc) =>
                maybeDoc match {
                  case Some(doc) => entriesResponse(format(mongoDocToEntry(doc)))
                  case None => authNotFoundResponse
                }
              case Failure(ex) =>
                system.log.error(s"Cannot extract entries from db due to $ex")
                internalServerErrorResponse
            }
          }
        }
    } ~ post {
      pathPrefix("add-admin") {
        parameter("auth", "hostname") {
          (auth, hostname) =>
            onComplete(db.getEntryByAuthAndHostname(auth, hostname)) {
              case Success(Some(_)) => adminAlreadyExistsResponse
              case Success(None) =>
                onComplete(db.insertSingleEntry(Entries(auth, hostname, isAdmin = true, -1))) {
                  case Success(Some(_)) => okResponse
                  case _ =>
                    system.log.error(s"Cannot insert entries to db")
                    internalServerErrorResponse
                }
              case Failure(ex) =>
                system.log.error(s"Cannot get entries due to $ex")
                internalServerErrorResponse
            }

        }
      } ~ pathPrefix("add-client") {
        parameter("auth", "hostname", "quota".as[Int]) {
          (auth, hostname, quota) =>
            onComplete(db.getEntryByAuthAndHostname(auth, hostname)) {
              case Success(Some(_)) => clientAlreadyExistsResponse
              case Success(None) =>
                onComplete(db.insertSingleEntry(Entries(auth, hostname, isAdmin = false, quota))) {
                  case Success(Some(_)) => okResponse
                  case _ =>
                    system.log.error(s"Cannot insert entries to db")
                    internalServerErrorResponse
                }
              case Failure(ex) =>
                system.log.error(s"Cannot get entries due to $ex")
                internalServerErrorResponse
            }

        }
      } ~ pathPrefix("set-quota") {
        parameter("hostname", "quotaToSet".as[Int]) { (hostname, quotaToSet) =>
          onComplete(db.getEntryByHostname(hostname)) {
            case Success(Some(_)) =>
              onComplete(db.updateQuota(hostname, quotaToSet)) {
                case Success(Some(_)) => okResponse
                case _ =>
                  system.log.error(s"Cannot insert entries to db")
                  internalServerErrorResponse
              }
            case Success(None) => hostnameNotFoundResponse

            case Failure(ex) =>
              system.log.error(s"Cannot get entries due to $ex")
              internalServerErrorResponse
          }
        }
      }
    } ~ delete {
      pathPrefix("delete-admin") {
        parameter("auth") { auth =>
          onComplete(db.getEntryByAuth(auth)) {
            case Success(Some(_)) =>
              onComplete(db.deleteByAuth(auth)) {
                case Success(Some(_)) => okResponse
                case _ =>
                  system.log.error(s"Cannot delete auth")
                  internalServerErrorResponse
              }
            case Success(None) => authNotFoundResponse

            case Failure(ex) =>
              system.log.error(s"Cannot delete hostname due to $ex")
              internalServerErrorResponse
          }

        }
      } ~ pathPrefix("delete-client") {
        parameter("hostname") { hostname =>
          onComplete(db.getEntryByHostname(hostname)) {
            case Success(Some(_)) =>
              onComplete(db.deleteByHostname(hostname)) {
                case Success(Some(_)) => okResponse
                case _ =>
                  system.log.error(s"Cannot delete hostname")
                  internalServerErrorResponse
              }
            case Success(None) => hostnameNotFoundResponse

            case Failure(ex) =>
              system.log.error(s"Cannot delete hostname due to $ex")
              internalServerErrorResponse
          }
        }
      }
    }
}
