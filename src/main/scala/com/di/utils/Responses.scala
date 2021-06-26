package com.di.utils

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpHeader, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.{Route, StandardRoute}
import com.di.jsonFormatters.JsonWriter
import com.di.routes.{ClientApiRoute, GlobalRoute}

object Responses extends BaseHttp {

  def okResponse: StandardRoute = complete(standardOkResponse("ok"))
  def deepPingResponse: StandardRoute = complete(standardOkResponse("db ok"))
  def maxLimitResponse(limit: Int): StandardRoute = complete(standardOkResponse(limit))
  def entriesResponse(entries: String): StandardRoute = complete(okResponseAsIs(entries))

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
      status = StatusCodes.BadRequest,
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
    complete(notFoundResponse("Given host is not found"))

  def authNotFoundResponse: StandardRoute =
    complete(notFoundResponse("Given auth is not found"))

  def invalidUrlResponse: StandardRoute =
    complete(notFoundResponse("Url is malformed"))

  def adminAlreadyExistsResponse: StandardRoute =
    complete(alreadyExistsResponse("Given admin already exists"))

  def clientAlreadyExistsResponse: StandardRoute =
    complete(alreadyExistsResponse("Given client already exists"))
}
