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
  def deepPingResponse: StandardRoute = complete(standardOkResponse("auth successful"))
  def maxLimitResponse(limit: Int): StandardRoute = complete(standardOkResponse(limit))
  def entriesResponse(entries: String): StandardRoute = complete(standardOkResponse(entries))

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

  def authenticationFailedResponse: StandardRoute =
    complete(HttpResponse(
      status = StatusCodes.PreconditionFailed,
      headers = baseHeaders,
      entity = HttpEntity(
        contentType = ContentTypes.`application/json`,
        string = JsonWriter.format("Authentication failed")
      )))

  def internalServerErrorResponse: StandardRoute =
    complete(HttpResponse(
      status = StatusCodes.InternalServerError,
      headers = baseHeaders,
      entity = HttpEntity(
        contentType = ContentTypes.`application/json`,
        string = JsonWriter.format("Internal server error")
      )))

  def authMechanismIsNotWorkingResponse(message: String): StandardRoute =
    complete(HttpResponse(
      status = StatusCodes.ExpectationFailed,
      headers = baseHeaders,
      entity = HttpEntity(
        contentType = ContentTypes.`application/json`,
        string = JsonWriter.format(message)
      )))

  def hostnameNotFoundResponse: StandardRoute =
    complete(HttpResponse(
      status = StatusCodes.NotFound,
      headers = baseHeaders,
      entity = HttpEntity(
        contentType = ContentTypes.`application/json`,
        string = JsonWriter.format("Given host is not found")
      )))
}
