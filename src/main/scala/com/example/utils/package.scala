package com.example

import com.example.db.Entries
import org.mongodb.scala.bson.collection.immutable.Document

import scala.util.{Failure, Success, Try}


package object utils {

  /**
   * Transforms Try[Option[T]] to Some(T) if Success and to None if failed
   */
  def tryOptionToOption[T](func: Try[Option[T]]): Option[T] = func match {
    case Success(res) => res
    case Failure(_) => None
  }

  /**
   * Transforms  Try(value) to Option(value) OR
   * alternative Failure(ex) to Some(alternative) to prevent throwing errors
   */
  def tryOptionWithAlternative[T](func: Try[Option[T]])(alternateResult: T): Option[T] = func match {
    case Success(res) => res
    case Failure(_) => Some(alternateResult)
  }

  def mongoDocToEntry(doc: Document): Entries =
    Entries(
      doc("auth_entry").asString().getValue,
      doc("hostname").asString().getValue,
      doc("is_admin").asBoolean().getValue,
      doc("actual_quota").asDouble().getValue
    )

}
