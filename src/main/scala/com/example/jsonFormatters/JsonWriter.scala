package com.example.jsonFormatters

import org.json4s.{JArray, JInt, JObject, JString, JValue}
import org.json4s.jackson.JsonMethods.{compact, pretty, render}

object JsonWriter {

  def format[T](result: T): String = result match {
    case result: Int => pretty(render(JObject(
      "max-limit" -> JInt(result)
    )))
    case result: String if result == "pong" =>
      compact(render(JObject(
        "message" -> JString(result)
      )))
  }
}

