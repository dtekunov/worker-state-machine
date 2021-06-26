package com.di.db

import java.time.LocalDateTime

case class UserLogs(id: Long,
                    hostname: String,
                    addedTime: LocalDateTime,
                    quotaReserved: Int)

object UserLogs {
  val idDb = "id"
  val hostnameDb = "hostname"
  val addedTimeDb = "added_time"
  val quotaReservedDb = "quota_reserved"
}