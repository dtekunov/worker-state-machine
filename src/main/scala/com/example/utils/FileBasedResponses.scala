package com.example.utils

import akka.http.scaladsl.server.directives._
import ContentTypeResolver.Default
import akka.http.scaladsl.server.Directives.getFromFile
import akka.http.scaladsl.server.Route

object FileBasedResponses extends BaseHttp {

  def smallFileResponse(filename: String): Route = getFromFile(s"$filename")

}
