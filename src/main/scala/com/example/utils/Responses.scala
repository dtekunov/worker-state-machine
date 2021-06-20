package com.example.utils

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpHeader, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.StandardRoute
import com.example.jsonFormatters.JsonWriter

object Responses {

  private final val baseHeaders = Vector.empty[HttpHeader]

  private def standardOkResponse[T](output: T): HttpResponse =
    HttpResponse(
      status = StatusCodes.OK,
      headers = baseHeaders,
      entity = HttpEntity(
        contentType = ContentTypes.`application/json`,
        string = JsonWriter.format(output)
      ))

  def pongResponse: StandardRoute = complete(standardOkResponse("pong"))
  def maxLimitResponse(limit: Int): StandardRoute = complete(standardOkResponse(limit))

  def notAcceptableResponse(message: String): StandardRoute =
    complete(HttpResponse(
      status = StatusCodes.NotAcceptable,
      headers = baseHeaders,
      entity = HttpEntity(
        contentType = ContentTypes.`application/json`,
        string = JsonWriter.format(message)
      )))

  def invalidClientEntityResponse: StandardRoute =
    complete(HttpResponse(
      status = StatusCodes.Unauthorized,
      headers = baseHeaders,
      entity = HttpEntity(
        contentType = ContentTypes.`application/json`,
        string = JsonWriter.format("Invalid `Client-Entity`")
      )))
}
