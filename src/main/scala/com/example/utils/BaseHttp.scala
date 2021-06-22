package com.example.utils

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpHeader, HttpResponse, StatusCodes}
import com.example.jsonFormatters.JsonWriter

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
}
