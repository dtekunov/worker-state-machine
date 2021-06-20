package com.example

import akka.http.scaladsl.server.{Directive0, Directive1}
import akka.http.scaladsl.server.directives.BasicDirectives.provide
import akka.http.scaladsl.server.directives.RouteDirectives.complete

package object directives {

  trait Requester
  case object Client  extends Requester
  case object Admin   extends Requester
  case object Editor  extends Requester
  case object Unknown extends Requester

  def checkRequester(rawRequester: String): Directive1[Requester] = rawRequester.toLowerCase match {
    case requester if requester == "client" => provide(Client)
    case requester if requester == "admin" =>  provide(Admin)
    case requester if requester == "editor" => provide(Editor)
    case _ => provide(Unknown)
  }
}
