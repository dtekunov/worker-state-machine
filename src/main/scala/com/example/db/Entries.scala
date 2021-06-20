package com.example.db

import slick.jdbc.SQLiteProfile.api._
import slick.lifted.{ProvenShape, Tag}

case class Entries(authEntry: String,
                   hostname: String)

class EntriesTable(tag: Tag) extends Table[Entries](tag, "entries") {

  def authEntry = column[String]("authEntry", O.PrimaryKey)
  def hostname  = column[String]("hostname")

  def * : ProvenShape[Entries] = (authEntry, hostname).mapTo[Entries]
}
