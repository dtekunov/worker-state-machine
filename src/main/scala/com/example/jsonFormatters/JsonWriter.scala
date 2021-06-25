package com.example.jsonFormatters

import com.example.db.{Entries, UserLogs}
import org.json4s.{Extraction, FullTypeHints, JArray, JBool, JDouble, JInt, JLong, JObject, JString, JValue}
import org.json4s.jackson.JsonMethods.{compact, pretty, render}
import org.json4s.jackson.Serialization

object JsonWriter {

  def format[T](result: T): String = result match {
    case result: Int => pretty(render(JObject(
      "max-limit" -> JInt(result)
    )))
    case result: String =>
      compact(render(JObject(
        "message" -> JString(result)
      )))

    case res: Entries =>
      pretty(render(JObject(
        Entries.authEntryDb -> JString(res.authEntry),
        Entries.hostnameDb -> JString(res.hostname),
        Entries.isAdminDb -> JBool(res.isAdmin),
        Entries.actualQuotaDb -> JDouble(res.actualQuota)
      )))

    case res: UserLogs =>
      pretty(render(JObject(
        "id" -> JLong(res.id),
        "hostname" -> JString(res.hostname),
        "added_time" -> JString(res.addedTime.toString),
        "quota_reserved" -> JInt(res.quotaReserved),
      )))
  }

  def formatEntrySeq(toTransform: Seq[Entries]): String = {
    implicit val formats = Serialization.formats(FullTypeHints(List(classOf[Entries])))
    pretty(render(Extraction.decompose(toTransform)).removeField {
      case ("jsonClass", _) => true
      case _ => false
    })
  }
}

