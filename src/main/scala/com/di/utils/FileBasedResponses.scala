package com.di.utils

import akka.http.scaladsl.server.directives._
import ContentTypeResolver.Default
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives.{complete, getFromFile}
import akka.http.scaladsl.server.{Route, StandardRoute}
import com.di.jsonFormatters.JsonWriter
import com.di.utils.Responses.baseHeaders

object FileBasedResponses extends BaseHttp {

  def getFileResponse(filename: String): Route = getFromFile(s"$filename") //TODO: check that file exists

  def fileUploadedResponse(filename: String): StandardRoute = complete(HttpResponse(
    status = StatusCodes.BandwidthLimitExceeded,
    headers = baseHeaders,
    entity = HttpEntity(
      contentType = ContentTypes.`application/json`,
      string = JsonWriter.format(s"File $filename uploaded")
    )))

  val quotaOverflowedResponse: StandardRoute = complete(HttpResponse(
    status = StatusCodes.BandwidthLimitExceeded,
    headers = baseHeaders,
    entity = HttpEntity(
      contentType = ContentTypes.`application/json`,
      string = JsonWriter.format("Quota limit reached")
    )))
}
