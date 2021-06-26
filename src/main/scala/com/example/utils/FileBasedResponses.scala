package com.example.utils

import akka.http.scaladsl.server.directives._
import ContentTypeResolver.Default
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives.{complete, getFromFile}
import akka.http.scaladsl.server.{Route, StandardRoute}
import com.example.jsonFormatters.JsonWriter
import com.example.utils.Responses.baseHeaders

object FileBasedResponses extends BaseHttp {

  def smallFileResponse(filename: String): Route = getFromFile(s"$filename") //TODO: check that file exists

  val quotaOverflowedResponse: StandardRoute = complete(HttpResponse(
    status = StatusCodes.BandwidthLimitExceeded,
    headers = baseHeaders,
    entity = HttpEntity(
      contentType = ContentTypes.`application/json`,
      string = JsonWriter.format("Quota limit reached")
    )))

}
