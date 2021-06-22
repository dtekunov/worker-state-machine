package com.example.db

import slick.jdbc.SQLiteProfile.api._
import slick.lifted.{ProvenShape, Tag}

case class Entries(authEntry: String,
                   hostname: String,
                   isAdmin: Boolean)

