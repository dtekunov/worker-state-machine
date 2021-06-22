package com.example.db

import slick.jdbc.SQLiteProfile.api._
import slick.lifted.Tag

import java.time.{LocalDate, LocalDateTime, LocalTime}
import java.sql.Timestamp

case class UserLogs(id: Long,
                    hostname: String,
                    addedTime: LocalDateTime,
                    quota_reserved: Int)