package com.example.db

import slick.jdbc.SQLiteProfile.api._
import slick.lifted.Tag

import java.time.{LocalDate, LocalTime}
import java.sql.Timestamp

case class UserLogs(id: Long,
                    hostname: String,
                    quota_reserved: Int)

class UserLogsTable(tag: Tag) extends Table[UserLogs](tag, "user_logs") {


  def id             = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def hostname       = column[String]("hostname")
//  def request_time   = column[TimeStamp]("request_time")
  def quota_reserved = column[Int]("quota_reserved")

  def * = (id, hostname, quota_reserved).mapTo[UserLogs]
}