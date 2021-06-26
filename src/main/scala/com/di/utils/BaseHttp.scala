package com.di.utils

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpHeader, HttpResponse, StatusCodes}
import com.di.jsonFormatters.JsonWriter

trait BaseHttp {

  protected final val baseHeaders = Vector.empty[HttpHeader]

  protected def standardOkResponse[T](output: T): HttpResponse =
    HttpResponse(
      status = StatusCodes.OK,
      headers = baseHeaders,
      entity = HttpEntity(
        contentType = ContentTypes.`application/json`,
        string = JsonWriter.format(output)
      ))

  protected def okResponseAsIs(output: String): HttpResponse =
    HttpResponse(
      status = StatusCodes.OK,
      headers = baseHeaders,
      entity = HttpEntity(
        contentType = ContentTypes.`application/json`,
        string = output
      ))

  protected def alreadyExistsResponse(output: String): HttpResponse =
   HttpResponse(
      status = StatusCodes.AlreadyReported,
      headers = baseHeaders,
      entity = HttpEntity(
        contentType = ContentTypes.`application/json`,
        string = JsonWriter.format(output)
      ))

  protected def notFoundResponse(output: String): HttpResponse =
    HttpResponse(
      status = StatusCodes.AlreadyReported,
      headers = baseHeaders,
      entity = HttpEntity(
        contentType = ContentTypes.`application/json`,
        string = JsonWriter.format(output)
      ))
}
