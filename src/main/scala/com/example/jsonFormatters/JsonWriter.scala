package com.example.jsonFormatters

import com.example.db.Entries
import org.json4s.{Extraction, FullTypeHints, JArray, JInt, JObject, JString, JValue}
import org.json4s.jackson.JsonMethods.{compact, pretty, render}
import org.json4s.jackson.Serialization

object JsonWriter {

  def format[T](result: T): String = result match {
    case result: Int => pretty(render(JObject(
      "max-limit" -> JInt(result)
    )))
    case result: String if result == "pong" =>
      compact(render(JObject(
        "message" -> JString(result)
      )))

    case res: Entries =>
      compact(render(JObject(
        "auth_entry" -> JString(res.authEntry),
        "hostname" -> JString(res.hostname)
      )))
  }

  def formatEntrySeq(toTransform: Seq[Entries]) = {
    implicit val formats = Serialization.formats(FullTypeHints(List(classOf[Entries])))
    pretty(render(Extraction.decompose(toTransform)).removeField {
      case ("jsonClass", _) => true
      case _ => false
    })
  }
}

